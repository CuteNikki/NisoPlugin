package moe.niso.managers;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TeleportAskManager {
    private static final NisoPlugin plugin = NisoPlugin.getInstance();
    // receiver -> (sender -> requestDetails)
    private static final Map<UUID, Map<UUID, TeleportRequest>> requests = new HashMap<>();

    /**
     * Send a teleport request (either normal tpask or tphere).
     */
    public static void sendRequest(Player sender, Player target, boolean isHereRequest) {
        UUID targetId = target.getUniqueId();
        UUID senderId = sender.getUniqueId();

        Map<UUID, TeleportRequest> requestsForTarget = requests.computeIfAbsent(targetId, k -> new HashMap<>());

        if (requestsForTarget.containsKey(senderId)) {
            TeleportRequest existing = requestsForTarget.get(senderId);

            if (System.currentTimeMillis() < existing.expirationTime()) {
                long secondsLeft = (existing.expirationTime() - System.currentTimeMillis()) / 1000;
                sender.sendMessage(plugin.prefixMessage(Component.text("You already have a pending request to").color(NamedTextColor.RED)
                        .appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.RED))
                        .appendSpace().append(Component.text("Please wait " + secondsLeft + " seconds.").color(NamedTextColor.RED))));
                return;
            } else {
                requestsForTarget.remove(senderId);
            }
        }

        long expirationTime = System.currentTimeMillis() + 60_000; // 60s expiration
        requestsForTarget.put(senderId, new TeleportRequest(expirationTime, isHereRequest));

        if (isHereRequest) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Teleport-here request sent to").color(NamedTextColor.GREEN).appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW))));
            target.sendMessage(plugin.prefixMessage(Component.text(sender.getName()).color(NamedTextColor.YELLOW).append(Component.text(" has requested that you teleport to them.").color(NamedTextColor.GREEN)))
                    .appendNewline()
                    .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text("Accept").color(NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text("Click to teleport to " + sender.getName()).color(NamedTextColor.GREEN))).clickEvent(ClickEvent.runCommand("/tpaccept " + sender.getName())))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY))
                    .appendSpace()
                    .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text("Deny").color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text("Click to deny request from " + sender.getName()).color(NamedTextColor.RED))).clickEvent(ClickEvent.runCommand("/tpdeny " + sender.getName())))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY)));
        } else {
            sender.sendMessage(plugin.prefixMessage(Component.text("Teleport request sent to").color(NamedTextColor.GREEN).appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW))));
            target.sendMessage(plugin.prefixMessage(Component.text("Teleport request received from").color(NamedTextColor.GREEN).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW)))
                    .appendNewline()
                    .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text("Accept").color(NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text("Click to accept request from " + sender.getName()).color(NamedTextColor.GREEN))).clickEvent(ClickEvent.runCommand("/tpaccept " + sender.getName())))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY))
                    .appendSpace()
                    .append(Component.text("[").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text("Deny").color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text("Click to deny request from " + sender.getName()).color(NamedTextColor.RED))).clickEvent(ClickEvent.runCommand("/tpdeny " + sender.getName())))
                    .append(Component.text("]").color(NamedTextColor.DARK_GRAY)));
        }

        // Schedule a task to check for expiration
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<UUID, TeleportRequest> currentRequests = requests.get(targetId);

                if (currentRequests != null && currentRequests.containsKey(senderId)) {
                    TeleportRequest req = currentRequests.get(senderId);

                    if (System.currentTimeMillis() >= req.expirationTime()) {
                        currentRequests.remove(senderId);

                        if (currentRequests.isEmpty()) requests.remove(targetId);

                        sender.sendMessage(plugin.prefixMessage(Component.text("Teleport request to").color(NamedTextColor.RED).appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW)).appendSpace().append(Component.text("has expired.").color(NamedTextColor.RED))));
                        target.sendMessage(plugin.prefixMessage(Component.text("Teleport request from").color(NamedTextColor.RED).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW)).appendSpace().append(Component.text("has expired.").color(NamedTextColor.RED))));
                    }
                }
            }
        }.runTaskLater(plugin, 20 * 61); // Check after 61 seconds because we set expiration to 60 seconds
    }

    /**
     * Accept a teleport request from sender to receiver.
     *
     * @param receiver the player receiving the request
     * @param sender   the player sending the request
     */
    public static void acceptRequest(Player receiver, Player sender) {
        Map<UUID, TeleportRequest> requestsForTarget = requests.get(receiver.getUniqueId());

        if (requestsForTarget != null && requestsForTarget.containsKey(sender.getUniqueId())) {
            TeleportRequest request = requestsForTarget.get(sender.getUniqueId());

            if (request.isHereRequest()) {
                // Receiver teleports to Sender
                receiver.teleport(sender.getLocation());

                receiver.sendMessage(plugin.prefixMessage(Component.text("You have been teleported to").color(NamedTextColor.GREEN).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))));
                sender.sendMessage(plugin.prefixMessage(Component.text(receiver.getName()).color(NamedTextColor.YELLOW).appendSpace().append(Component.text("has teleported to you.").color(NamedTextColor.GREEN))));
            } else {
                // Sender teleports to Receiver
                sender.teleport(receiver.getLocation());

                sender.sendMessage(plugin.prefixMessage(Component.text("You have been teleported to").color(NamedTextColor.GREEN).appendSpace().append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW))));
                receiver.sendMessage(plugin.prefixMessage(Component.text("You have teleported").color(NamedTextColor.GREEN).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))));
            }

            removeRequest(receiver, sender);
        }
    }

    /**
     * Deny a teleport request from sender to receiver.
     *
     * @param receiver the player receiving the request
     * @param sender   the player sending the request
     */
    public static void denyRequest(Player receiver, Player sender) {
        Map<UUID, TeleportRequest> requestsForTarget = requests.get(receiver.getUniqueId());

        if (requestsForTarget != null && requestsForTarget.containsKey(sender.getUniqueId())) {
            receiver.sendMessage(plugin.prefixMessage(Component.text("Teleport request from").color(NamedTextColor.RED).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW)).appendSpace().append(Component.text("has been denied.").color(NamedTextColor.RED))));
            sender.sendMessage(plugin.prefixMessage(Component.text("Teleport request to").color(NamedTextColor.RED).appendSpace().append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW)).appendSpace().append(Component.text("has been denied.").color(NamedTextColor.RED))));

            removeRequest(receiver, sender);
        }
    }

    /**
     * Check if no active teleport request exists from sender to receiver.
     */
    public static boolean hasNoRequest(Player receiver, Player sender) {
        Map<UUID, TeleportRequest> requestsForTarget = requests.get(receiver.getUniqueId());
        if (requestsForTarget == null || !requestsForTarget.containsKey(sender.getUniqueId())) {
            return true;
        }
        TeleportRequest request = requestsForTarget.get(sender.getUniqueId());
        if (System.currentTimeMillis() >= request.expirationTime()) {
            removeRequest(receiver, sender);
            return true;
        }
        return false;
    }

    public static void removeRequest(Player receiver, Player sender) {
        Map<UUID, TeleportRequest> requestsForTarget = requests.get(receiver.getUniqueId());
        if (requestsForTarget != null) {
            requestsForTarget.remove(sender.getUniqueId());
            if (requestsForTarget.isEmpty()) requests.remove(receiver.getUniqueId());
        }
    }

    /**
     * Retrieves active, online pending senders for a receiver, automatically cleaning up expired ones.
     */
    public static List<Player> getValidPendingRequests(Player receiver) {
        Map<UUID, TeleportRequest> requestsForTarget = requests.get(receiver.getUniqueId());
        if (requestsForTarget == null || requestsForTarget.isEmpty()) {
            return List.of();
        }

        long now = System.currentTimeMillis();
        List<Player> senders = new ArrayList<>();

        Iterator<Map.Entry<UUID, TeleportRequest>> iterator = requestsForTarget.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, TeleportRequest> entry = iterator.next();
            if (now >= entry.getValue().expirationTime()) {
                iterator.remove();
                continue;
            }
            Player sender = plugin.getServer().getPlayer(entry.getKey());
            if (sender != null && sender.isOnline()) {
                senders.add(sender);
            }
        }

        if (requestsForTarget.isEmpty()) {
            requests.remove(receiver.getUniqueId());
        }

        return senders;
    }

    public static List<String> getRequests(Player player) {
        return getValidPendingRequests(player).stream()
                .map(Player::getName)
                .toList();
    }

    public record TeleportRequest(long expirationTime, boolean isHereRequest) {
    }
}