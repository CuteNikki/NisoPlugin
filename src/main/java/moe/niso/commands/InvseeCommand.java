package moe.niso.commands;

import moe.niso.NisoPlugin;
import moe.niso.gui.InvseeHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InvseeCommand implements TabExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.invsee.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.prefixMessage(Component.text("Usage: /invsee <player>").color(NamedTextColor.RED)));
            return true;
        }

        Player targetPlayer = plugin.getServer().getPlayer(args[0]);

        if (targetPlayer == null) {
            player.sendMessage(plugin.prefixMessage(Component.text("Player not found!").color(NamedTextColor.RED)));
            return true;
        }

        if (targetPlayer.hasPermission("niso.invsee.bypass")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You cannot open this player's inventory!").color(NamedTextColor.RED)));
            return true;
        }

        InvseeHolder holder = new InvseeHolder(targetPlayer);
        Inventory gui = plugin.getServer().createInventory(holder, 54, Component.text(targetPlayer.getName() + "'s Inventory").color(NamedTextColor.DARK_GRAY));
        holder.setInventory(gui);

        populateGui(gui, targetPlayer);

        player.openInventory(gui);
        player.sendMessage(plugin.prefixMessage(Component.text("Opened " + targetPlayer.getName() + "'s inventory!").color(NamedTextColor.GREEN)));
        return true;
    }

    public static void populateGui(Inventory gui, Player target) {
        PlayerInventory targetInv = target.getInventory();

        // Top Row: Armor (0-3) & Off-hand (4)
        gui.setItem(0, targetInv.getHelmet());
        gui.setItem(1, targetInv.getChestplate());
        gui.setItem(2, targetInv.getLeggings());
        gui.setItem(3, targetInv.getBoots());
        gui.setItem(4, targetInv.getItemInOffHand());

        // Filler Glass (5-8)
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            filler.setItemMeta(meta);
        }
        for (int i = 5; i < 9; i++) {
            gui.setItem(i, filler);
        }

        // Main Storage (Minecraft storage slots 9..35 mapped to GUI 9..35)
        ItemStack[] storage = targetInv.getStorageContents();
        for (int i = 9; i < 36; i++) {
            gui.setItem(i, storage[i]);
        }

        // Hotbar (Minecraft hotbar slots 0..8 mapped to GUI 36..44)
        for (int i = 0; i < 9; i++) {
            gui.setItem(36 + i, storage[i]);
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
