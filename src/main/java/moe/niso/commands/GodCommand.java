package moe.niso.commands;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GodCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.god.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("niso.god.others")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
                return true;
            }

            if (targetPlayer.hasPermission("niso.god.bypass")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You cannot toggle god mode for this player!").color(NamedTextColor.RED)));
                return true;
            }

            if (targetPlayer.isInvulnerable()) {
                targetPlayer.setInvulnerable(false);
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("God mode disabled!").color(NamedTextColor.RED)));
                player.sendMessage(plugin.prefixMessage(Component.text(targetPlayer.getName() + "'s god mode has been disabled!").color(NamedTextColor.RED)));
            } else {
                targetPlayer.setInvulnerable(true);
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("God mode enabled!").color(NamedTextColor.GREEN)));
                player.sendMessage(plugin.prefixMessage(Component.text(targetPlayer.getName() + "'s god mode has been enabled!").color(NamedTextColor.GREEN)));
            }

            return true;
        }

        if (player.isInvulnerable()) {
            player.setInvulnerable(false);
            player.sendMessage(plugin.prefixMessage(Component.text("God mode disabled!").color(NamedTextColor.RED)));
        } else {
            player.setInvulnerable(true);
            player.sendMessage(plugin.prefixMessage(Component.text("God mode enabled!").color(NamedTextColor.GREEN)));
        }

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
