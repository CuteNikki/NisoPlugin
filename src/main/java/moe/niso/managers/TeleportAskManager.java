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

    // receiver -> (sender -> expiration)
    private static final Map<UUID, Map<UUID, Long>> requests = new HashMap<>();

    /**
     * Send a teleport request from sender to target.
     *
     * @param sender the player sending the request
     * @param target the player receiving the request
     */
    public static void sendRequest(Player sender, Player target) {
        UUID targetId = target.getUniqueId();
        UUID senderId = sender.getUniqueId();

        Map<UUID, Long> requestsForTarget = requests.computeIfAbsent(targetId, k -> new HashMap<>());

        // Check if the sender has a pending request to the target
        if (requestsForTarget.containsKey(senderId)) {
            long expiresAt = requestsForTarget.get(senderId);

            if (System.currentTimeMillis() < expiresAt) {
                long secondsLeft = (expiresAt - System.currentTimeMillis()) / 1000;
                sender.sendMessage(plugin.prefixMessage(Component.text("You already have a pending request to").color(NamedTextColor.RED).appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW)).append(Component.text(".").color(NamedTextColor.RED)).appendSpace().append(Component.text("Please wait " + secondsLeft + " seconds.").color(NamedTextColor.RED))));
                return;
            } else {
                // expired but still stored - remove it
                requestsForTarget.remove(senderId);
            }
        }

        // Add new request
        long expirationTime = System.currentTimeMillis() + 60_000; // 60 seconds expiration
        requestsForTarget.put(senderId, expirationTime);

        sender.sendMessage(plugin.prefixMessage(Component.text("Teleport request sent to").color(NamedTextColor.GREEN).appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW))));
        target.sendMessage(plugin.prefixMessage(Component.text("Teleport request received from").color(NamedTextColor.GREEN).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))).appendNewline().append(Component.text("[").color(NamedTextColor.DARK_GRAY)).append(Component.text("Accept").color(NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text("Click to accept request from " + sender.getName()).color(NamedTextColor.GREEN))).clickEvent(ClickEvent.runCommand("/tpaccept " + sender.getName()))).append(Component.text("]").color(NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("[").color(NamedTextColor.DARK_GRAY)).append(Component.text("Deny").color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text("Click to deny request from " + sender.getName()).color(NamedTextColor.RED))).clickEvent(ClickEvent.runCommand("/tpdeny " + sender.getName()))).append(Component.text("]").color(NamedTextColor.DARK_GRAY)));

        // Schedule a task to check for expiration
        new BukkitRunnable() {
            @Override
            public void run() {
                Map<UUID, Long> currentRequests = requests.get(targetId);

                if (currentRequests != null && currentRequests.containsKey(senderId)) {
                    long expiration = currentRequests.get(senderId);

                    if (System.currentTimeMillis() >= expiration) {
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
        Map<UUID, Long> requestsForTarget = requests.get(receiver.getUniqueId());

        if (requestsForTarget != null && requestsForTarget.containsKey(sender.getUniqueId())) {
            sender.teleport(receiver.getLocation());

            sender.sendMessage(plugin.prefixMessage(Component.text("You have been teleported to").color(NamedTextColor.GREEN).appendSpace().append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW))));
            receiver.sendMessage(plugin.prefixMessage(Component.text("You have teleported").color(NamedTextColor.GREEN).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW))));

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
        Map<UUID, Long> requestsForTarget = requests.get(receiver.getUniqueId());

        if (requestsForTarget != null && requestsForTarget.containsKey(sender.getUniqueId())) {
            receiver.sendMessage(plugin.prefixMessage(Component.text("Teleport request from").color(NamedTextColor.RED).appendSpace().append(Component.text(sender.getName()).color(NamedTextColor.YELLOW)).appendSpace().append(Component.text("has been denied.").color(NamedTextColor.RED))));
            sender.sendMessage(plugin.prefixMessage(Component.text("Teleport request to").color(NamedTextColor.RED).appendSpace().append(Component.text(receiver.getName()).color(NamedTextColor.YELLOW)).appendSpace().append(Component.text("has been denied.").color(NamedTextColor.RED))));

            removeRequest(receiver, sender);
        }
    }

    /**
     * Check if a teleport request exists between two players.
     *
     * @param receiver the player receiving the request
     * @param sender   the player sending the request
     * @return true if a request exists, false otherwise
     */
    public static boolean hasRequest(Player receiver, Player sender) {
        Map<UUID, Long> requestsForTarget = requests.get(receiver.getUniqueId());
        return requestsForTarget == null || !requestsForTarget.containsKey(sender.getUniqueId());
    }

    /**
     * Remove a teleport request between two players.
     *
     * @param receiver the player receiving the request
     * @param sender   the player sending the request
     */
    public static void removeRequest(Player receiver, Player sender) {
        Map<UUID, Long> requestsForTarget = requests.get(receiver.getUniqueId());
        if (requestsForTarget != null) {
            requestsForTarget.remove(sender.getUniqueId());
            if (requestsForTarget.isEmpty()) requests.remove(receiver.getUniqueId());
        }
    }

    /**
     * Get a list of players who have sent requests to the target player.
     *
     * @param player the target player
     * @return a list of player names who have sent requests
     */
    public static List<String> getRequests(Player player) {
        Map<UUID, Long> requestsForTarget = requests.get(player.getUniqueId());
        if (requestsForTarget != null) {
            return requestsForTarget.keySet().stream()
                    .map(uuid -> plugin.getServer().getPlayer(uuid))
                    .filter(Objects::nonNull)
                    .map(Player::getName)
                    .toList();
        }
        return List.of();
    }
}
