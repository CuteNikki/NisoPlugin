package moe.niso.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class InvseeHolder implements InventoryHolder {
    private final Player target;
    private Inventory inventory;

    public InvseeHolder(Player target) {
        this.target = target;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Player getTarget() {
        return target;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}