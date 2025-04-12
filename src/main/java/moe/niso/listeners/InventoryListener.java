package moe.niso.listeners;

import moe.niso.commands.TrashCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {
    @EventHandler
    public void onTrashClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inventory = event.getInventory();

        // Check if this inventory was one of our tracked trash bins
        if (TrashCommand.openTrashInventories.remove(player.getUniqueId()) == inventory) {
            inventory.clear(); // safely delete items in the trash inventory
        }
    }
}
