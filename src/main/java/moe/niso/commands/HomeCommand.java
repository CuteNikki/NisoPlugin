package moe.niso.commands;

import moe.niso.NisoPlugin;
import moe.niso.managers.HomeManager;
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

public class HomeCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.home.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 0 || !List.of("set", "delete", "teleport", "tp", "list").contains(args[0].toLowerCase())) {
            player.sendMessage(plugin.prefixMessage(Component.text("Usage: /home <set|delete|teleport|list> [name]").color(NamedTextColor.RED)));
            return true;
        }

        if (List.of("set", "delete", "teleport", "tp").contains(args[0].toLowerCase()) && args.length < 2) {
            player.sendMessage(plugin.prefixMessage(Component.text("Please provide a home name!").color(NamedTextColor.RED)));
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!player.hasPermission("niso.home.set")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final String homeName = args[1].toLowerCase();

            if (!HomeManager.isValidHomeName(homeName)) {
                player.sendMessage(plugin.prefixMessage(Component.text("Invalid home name! It must be alphanumeric and under 20 characters.").color(NamedTextColor.RED)));
                return true;
            }

            if (HomeManager.getHomeNames(player).size() >= HomeManager.getHomeLimit(player)) {
                player.sendMessage(plugin.prefixMessage(Component.text("You have reached the maximum number of homes!").color(NamedTextColor.RED)));
                return true;
            }

            final boolean isSet = HomeManager.setHome(player, homeName);

            if (!isSet) {
                player.sendMessage(plugin.prefixMessage(Component.text("An error occurred while setting your home. Please try again later.").color(NamedTextColor.RED)));
                return true;
            }

            player.sendMessage(plugin.prefixMessage(Component.text("Home ").color(NamedTextColor.GREEN).append(Component.text(homeName).color(NamedTextColor.YELLOW)).append(Component.text(" set!").color(NamedTextColor.GREEN))));
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (!player.hasPermission("niso.home.delete")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final String homeName = args[1].toLowerCase();
            final Location homeLocation = HomeManager.getHome(player, homeName);

            if (homeLocation == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Home ").color(NamedTextColor.RED).append(Component.text(homeName).color(NamedTextColor.YELLOW)).append(Component.text(" does not exist!").color(NamedTextColor.RED))));
                return true;
            }

            final boolean isDeleted = HomeManager.deleteHome(player, homeName);

            if (!isDeleted) {
                player.sendMessage(plugin.prefixMessage(Component.text("An error occurred while deleting your home. Please try again later.").color(NamedTextColor.RED)));
                return true;
            }

            player.sendMessage(plugin.prefixMessage(Component.text("Home ").color(NamedTextColor.GREEN).append(Component.text(args[1].toLowerCase()).color(NamedTextColor.YELLOW)).append(Component.text(" deleted!").color(NamedTextColor.GREEN))));
            return true;
        }

        if (List.of("teleport", "tp").contains(args[0].toLowerCase())) {

            if (!player.hasPermission("niso.home.teleport")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final String homeName = args[1].toLowerCase();
            final Location homeLocation = HomeManager.getHome(player, homeName);

            if (homeLocation == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Home ").color(NamedTextColor.RED).append(Component.text(homeName).color(NamedTextColor.YELLOW)).append(Component.text(" does not exist!").color(NamedTextColor.RED))));
                return true;
            }

            try {
                player.teleport(homeLocation);
                player.sendMessage(plugin.prefixMessage(Component.text("Teleported to home ").color(NamedTextColor.GREEN).append(Component.text(homeName).color(NamedTextColor.YELLOW)).append(Component.text("!").color(NamedTextColor.GREEN))));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to teleport " + player.getName() + " to home '" + homeName + "'");
                player.sendMessage(plugin.prefixMessage(Component.text("Failed to teleport to home ").color(NamedTextColor.RED).append(Component.text(homeName).color(NamedTextColor.YELLOW)).append(Component.text("!").color(NamedTextColor.RED))));
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {

            if (!player.hasPermission("niso.home.list")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            final List<String> homeNames = HomeManager.getHomeNames(player);

            if (homeNames.isEmpty()) {
                player.sendMessage(plugin.prefixMessage(Component.text("You have no homes set!").color(NamedTextColor.RED)));
                return true;
            }

            player.sendMessage(plugin.prefixMessage(Component.text("Your homes: ").color(NamedTextColor.GREEN).append(Component.text(String.join(", ", homeNames)).color(NamedTextColor.YELLOW))));
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
            if (!(sender instanceof Player player)) {
                return List.of();
            }

            return HomeManager.getHomeNames(player);
        }

        return List.of();
    }
}