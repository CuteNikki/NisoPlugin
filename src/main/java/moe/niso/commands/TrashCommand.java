package moe.niso.commands;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class TrashCommand implements CommandExecutor {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    // Keep track of open trash inventories (linked to players)
    public static final HashMap<UUID, Inventory> openTrashInventories = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.prefixMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED)));
            return true;
        }

        if (!player.hasPermission("niso.trash.use")) {
            player.sendMessage(plugin.prefixMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED)));
            return true;
        }

        Inventory trashInventory = Bukkit.createInventory(null, 27, Component.text("Trash").color(NamedTextColor.RED));
        openTrashInventories.put(player.getUniqueId(), trashInventory);
        player.openInventory(trashInventory);

        return true;
    }
}
