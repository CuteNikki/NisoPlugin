package moe.niso.listeners;

import moe.niso.NisoPlugin;
import moe.niso.gui.InvseeHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class InvseeListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof InvseeHolder holder)) {
            return;
        }

        Player target = holder.getTarget();

        // If target disconnected, cancel interactions
        if (!target.isOnline()) {
            event.setCancelled(true);
            return;
        }

        int slot = event.getRawSlot();

        // Block interaction with glass filler slots (5-8)
        if (slot >= 5 && slot <= 8) {
            event.setCancelled(true);
            return;
        }

        // Schedule sync right after the event resolves so Bukkit updates slot contents first
        NisoPlugin.getInstance().getServer().getScheduler().runTask(NisoPlugin.getInstance(), () -> {
            syncGuiToTarget(event.getInventory(), target);
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof InvseeHolder holder)) {
            return;
        }

        Player target = holder.getTarget();
        if (!target.isOnline()) {
            event.setCancelled(true);
            return;
        }

        // Prevent dragging across glass panes
        for (int slot : event.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                event.setCancelled(true);
                return;
            }
        }

        NisoPlugin.getInstance().getServer().getScheduler().runTask(NisoPlugin.getInstance(), () -> {
            syncGuiToTarget(event.getInventory(), target);
        });
    }

    private void syncGuiToTarget(Inventory gui, Player target) {
        PlayerInventory targetInv = target.getInventory();

        // 1. Sync Armor & Offhand
        targetInv.setHelmet(gui.getItem(0));
        targetInv.setChestplate(gui.getItem(1));
        targetInv.setLeggings(gui.getItem(2));
        targetInv.setBoots(gui.getItem(3));
        targetInv.setItemInOffHand(gui.getItem(4));

        // 2. Sync Main Inventory (GUI 9..35 -> Target 9..35)
        for (int i = 9; i < 36; i++) {
            targetInv.setItem(i, gui.getItem(i));
        }

        // 3. Sync Hotbar (GUI 36..44 -> Target 0..8)
        for (int i = 0; i < 9; i++) {
            targetInv.setItem(i, gui.getItem(36 + i));
        }

        target.updateInventory();
    }
}