package moe.niso.commands;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RepairCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.repair.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("niso.repair.others")) {
                player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
                return true;
            }

            Player targetPlayer = plugin.getServer().getPlayer(args[0]);

            if (targetPlayer == null) {
                player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
                return true;
            }

            ItemStack item = targetPlayer.getInventory().getItemInMainHand();

            if (item.getType().isAir()) {
                player.sendMessage(plugin.prefixMessage(Component.text("The player must be holding an item to repair!").color(NamedTextColor.RED)));
                return true;
            }

            if (item.getItemMeta() instanceof Damageable damageable) {
                damageable.setDamage(0);
                item.setItemMeta(damageable);
                targetPlayer.sendMessage(plugin.prefixMessage(Component.text("Your item has been repaired!").color(NamedTextColor.GREEN)));
                player.sendMessage(plugin.prefixMessage(Component.text("You repaired " + targetPlayer.getName() + "'s item!").color(NamedTextColor.GREEN)));
            } else {
                player.sendMessage(plugin.prefixMessage(Component.text("This item cannot be repaired!").color(NamedTextColor.RED)));
            }

            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(plugin.prefixMessage(Component.text("You must be holding an item to repair it!").color(NamedTextColor.RED)));
            return true;
        }

        if (item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(damageable);
            player.sendMessage(plugin.prefixMessage(Component.text("Your item has been repaired!").color(NamedTextColor.GREEN)));
        } else {
            player.sendMessage(plugin.prefixMessage(Component.text("This item cannot be repaired!").color(NamedTextColor.RED)));
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
