package moe.niso.commands;

import moe.niso.NisoPlugin;
import moe.niso.managers.TeleportAskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeleportListCommand implements CommandExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.tplist.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length != 0) {
            player.sendMessage(plugin.prefixMessage(Component.text("Usage: /tplist").color(NamedTextColor.RED)));
            return true;
        }

        List<String> requests = TeleportAskManager.getRequests(player);

        if (requests.isEmpty()) {
            player.sendMessage(plugin.prefixMessage(Component.text("You have no pending requests.").color(NamedTextColor.GREEN)));
            return true;
        }

        Component message = plugin.prefixMessage(Component.text("You have the following pending requests:").color(NamedTextColor.GREEN));
        for (String playerName : requests) {
            message = message.append(Component.newline()).append(Component.text("- " + playerName).color(NamedTextColor.YELLOW).appendSpace().append(Component.text("[").color(NamedTextColor.DARK_GRAY)).append(Component.text("Accept").color(NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text("Click to accept request from " + playerName).color(NamedTextColor.GREEN))).clickEvent(ClickEvent.runCommand("/tpaccept " + playerName))).append(Component.text("]").color(NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("[").color(NamedTextColor.DARK_GRAY)).append(Component.text("Deny").color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text("Click to deny request from " + playerName).color(NamedTextColor.RED))).clickEvent(ClickEvent.runCommand("/tpdeny " + playerName))).append(Component.text("]").color(NamedTextColor.DARK_GRAY)));
        }
        player.sendMessage(message);
        return true;
    }
}
