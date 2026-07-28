package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final NisoPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(NisoPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && !online.hasPermission("niso.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
        }
    }

    public boolean toggleVanish(Player player) {
        boolean newState = !isVanished(player.getUniqueId());
        setVanished(player, newState);
        return newState;
    }

    public void saveVanishedPlayers() {
        List<String> list = vanishedPlayers.stream().map(UUID::toString).toList();
        plugin.getConfig().set("vanished-players", list);
        plugin.saveConfig();
    }

    public void loadVanishedPlayers() {
        List<String> list = plugin.getConfig().getStringList("vanished-players");
        for (String uuidStr : list) {
            vanishedPlayers.add(UUID.fromString(uuidStr));
        }
    }
}
