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

public class EnderchestCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.enderchest.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("niso.enderchest.others")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
                return true;
            }

            if (targetPlayer.hasPermission("niso.enderchest.bypass")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You cannot open this player's ender chest!").color(NamedTextColor.RED)));
                return true;
            }

            player.openInventory(targetPlayer.getEnderChest());
            player.sendMessage(plugin.prefixMessage(Component.text("Opened " + targetPlayer.getName() + "'s ender chest!").color(NamedTextColor.GREEN)));
            return true;
        }

        player.openInventory(player.getEnderChest());
        player.sendMessage(plugin.prefixMessage(Component.text("Opened your ender chest!").color(NamedTextColor.GREEN)));

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
