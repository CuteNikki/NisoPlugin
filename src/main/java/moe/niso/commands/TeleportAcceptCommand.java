package moe.niso.commands;

import moe.niso.NisoPlugin;
import moe.niso.managers.TeleportAskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeleportAcceptCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.tpaccept.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.prefixMessage(Component.text("Usage: /tpaccept <player>").color(NamedTextColor.RED)));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.prefixMessage(Component.text("You cannot teleport to yourself!").color(NamedTextColor.RED)));
            return true;
        }

        if (TeleportAskManager.hasRequest(player, target)) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have a pending request from").color(NamedTextColor.RED).appendSpace().append(Component.text(target.getName()).color(NamedTextColor.YELLOW)).append(Component.text(".").color(NamedTextColor.RED))));
            return true;
        }

        TeleportAskManager.acceptRequest(player, target);
        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
