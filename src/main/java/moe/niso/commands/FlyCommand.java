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

public class FlyCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.fly.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("niso.fly.others")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
                return true;
            }

            if (targetPlayer.hasPermission("niso.fly.bypass")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You cannot toggle fly for this player!").color(NamedTextColor.RED)));
                return true;
            }

            toggleFly(player, targetPlayer);
            return true;
        }

        toggleFly(player, null);
        return true;
    }

    private void toggleFly(Player player, Player targetPlayer) {
        if (targetPlayer != null) {
            if (targetPlayer.getAllowFlight()) {
                targetPlayer.setAllowFlight(false);
                targetPlayer.setFlying(false);

                player.sendMessage(plugin.prefixMessage(Component.text("Fly mode disabled for " + targetPlayer.getName()).color(NamedTextColor.RED)));
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("Fly mode disabled!").color(NamedTextColor.RED)));
            } else {
                targetPlayer.setAllowFlight(true);
                targetPlayer.setFlying(true);

                player.sendMessage(plugin.prefixMessage(Component.text("Fly mode enabled for " + targetPlayer.getName()).color(NamedTextColor.GREEN)));
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("Fly mode enabled!").color(NamedTextColor.GREEN)));
            }
        } else {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);

                player.sendMessage(plugin.prefixMessage(Component.text("Fly mode disabled!").color(NamedTextColor.RED)));
            } else {
                player.setAllowFlight(true);
                player.setFlying(true);

                player.sendMessage(plugin.prefixMessage(Component.text("Fly mode enabled!").color(NamedTextColor.GREEN)));
            }
        }
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
