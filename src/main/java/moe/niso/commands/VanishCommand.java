package moe.niso.commands;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VanishCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.vanish.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("niso.vanish.others")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
                return true;
            }

            if (targetPlayer.hasPermission("niso.vanish.bypass")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You cannot toggle vanish for this player!").color(NamedTextColor.RED)));
                return true;
            }

            toggleVanish(player, targetPlayer);
            return true;
        }

        toggleVanish(player, null);
        return true;
    }

    private void toggleVanish(Player player, Player targetPlayer) {
        if (targetPlayer != null) {
            if (targetPlayer.hasMetadata("vanished")) {
                targetPlayer.removeMetadata("vanished", plugin);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.showPlayer(plugin, targetPlayer);
                }

                player.sendMessage(plugin.prefixMessage(Component.text("Vanish mode disabled for " + targetPlayer.getName()).color(NamedTextColor.GREEN)));
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("You are now visible!").color(NamedTextColor.GREEN)));
            } else {
                targetPlayer.setMetadata("vanished", new FixedMetadataValue(plugin, true));

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (!onlinePlayer.hasPermission("niso.vanish.see")) {
                        onlinePlayer.hidePlayer(plugin, targetPlayer);
                    }
                }

                player.sendMessage(plugin.prefixMessage(Component.text("Vanish mode enabled for " + targetPlayer.getName()).color(NamedTextColor.RED)));
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("You are now invisible!").color(NamedTextColor.GREEN)));
            }
        } else {
            if (player.hasMetadata("vanished")) {
                player.removeMetadata("vanished", plugin);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.showPlayer(plugin, player);
                }

                player.sendMessage(plugin.prefixMessage(Component.text("You are now visible!").color(NamedTextColor.GREEN)));
            } else {
                player.setMetadata("vanished", new FixedMetadataValue(plugin, true));

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.hidePlayer(plugin, player);
                }

                player.sendMessage(plugin.prefixMessage(Component.text("You are now invisible!").color(NamedTextColor.GREEN)));
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
