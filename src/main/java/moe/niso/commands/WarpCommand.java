package moe.niso.commands;

import moe.niso.NisoPlugin;
import moe.niso.managers.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WarpCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.warp.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 0 || !List.of("set", "delete", "teleport", "tp", "list").contains(args[0].toLowerCase())) {
            player.sendMessage(plugin.prefixMessage(Component.text("Usage: /warp <set|delete|teleport|list> [name]").color(NamedTextColor.RED)));
            return true;
        }

        if (List.of("set", "delete", "teleport", "tp").contains(args[0].toLowerCase()) && args.length < 2) {
            player.sendMessage(plugin.prefixMessage(Component.text("Please provide a warp name!").color(NamedTextColor.RED)));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!player.hasPermission("niso.warp.set")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final String warpName = args[1].toLowerCase();

            if (!WarpManager.isValidWarpName(warpName)) {
                player.sendMessage(plugin.prefixMessage(Component.text("Invalid warp name! It must be alphanumeric and under 20 characters.").color(NamedTextColor.RED)));
                return true;
            }

            final boolean isSet = WarpManager.setWarp(player, warpName);

            if (!isSet) {
                player.sendMessage(plugin.prefixMessage(Component.text("An error occurred while setting your warp. Please try again later.").color(NamedTextColor.RED)));
                return true;
            }

            player.sendMessage(plugin.prefixMessage(Component.text("Warp ").color(NamedTextColor.GREEN).append(Component.text(warpName).color(NamedTextColor.YELLOW)).append(Component.text(" has been set!").color(NamedTextColor.GREEN))));
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("niso.warp.delete")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final String warpName = args[1].toLowerCase();
            final Location warpLocation = WarpManager.getWarp(warpName);

            if (warpLocation == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Warp ").color(NamedTextColor.RED).append(Component.text(warpName).color(NamedTextColor.YELLOW)).append(Component.text(" does not exist!").color(NamedTextColor.RED))));
                return true;
            }

            final boolean isDeleted = WarpManager.deleteWarp(warpName);

            if (!isDeleted) {
                player.sendMessage(plugin.prefixMessage(Component.text("An error occurred while deleting the warp. Please try again later.").color(NamedTextColor.RED)));
                return true;
            }

            player.sendMessage(plugin.prefixMessage(Component.text("Warp ").color(NamedTextColor.GREEN).append(Component.text(warpName).color(NamedTextColor.YELLOW)).append(Component.text(" has been deleted!").color(NamedTextColor.GREEN))));
            return true;
        }

        if (List.of("teleport", "tp").contains(args[0].toLowerCase())) {

            if (!player.hasPermission("niso.warp.teleport")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final String warpName = args[1].toLowerCase();
            final Location warpLocation = WarpManager.getWarp(warpName);

            if (warpLocation == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Warp ").color(NamedTextColor.RED).append(Component.text(warpName).color(NamedTextColor.YELLOW)).append(Component.text(" does not exist or is invalid!").color(NamedTextColor.RED))));
                return true;
            }

            try {
                player.teleport(warpLocation);
                player.sendMessage(plugin.prefixMessage(Component.text("You have been teleported to warp ").color(NamedTextColor.GREEN).append(Component.text(warpName).color(NamedTextColor.YELLOW)).append(Component.text("!").color(NamedTextColor.GREEN))));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to teleport " + player.getName() + " to warp '" + warpName + "'");
                player.sendMessage(plugin.prefixMessage(Component.text("Failed to teleport to warp ").color(NamedTextColor.RED).append(Component.text(warpName).color(NamedTextColor.YELLOW)).append(Component.text("!").color(NamedTextColor.RED))));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {

            if (!player.hasPermission("niso.warp.list")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final List<String> warpNames = WarpManager.getWarpNames(true);

            if (warpNames.isEmpty()) {
                player.sendMessage(plugin.prefixMessage(Component.text("There are no warps!").color(NamedTextColor.RED)));
                return true;
            }

            player.sendMessage(plugin.prefixMessage(Component.text("Warps: ").color(NamedTextColor.GREEN).append(Component.text(String.join(", ", warpNames)).color(NamedTextColor.YELLOW))));
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 1) {
            return List.of("set", "delete", "list", "teleport", "tp");
        }

        if (args.length == 2 && List.of("teleport", "tp", "delete", "set").contains(args[0].toLowerCase())) {
            return WarpManager.getWarpNames(false).stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}