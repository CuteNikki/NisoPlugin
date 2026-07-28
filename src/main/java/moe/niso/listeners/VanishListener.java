package moe.niso.listeners;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import moe.niso.NisoPlugin;
import moe.niso.managers.VanishManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VanishListener implements Listener {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @EventHandler
    public void onServerListPing(PaperServerListPingEvent event) {
        VanishManager vanishManager = plugin.getVanishManager();

        // 1. Remove vanished players from the player sample hover list
        event.getListedPlayers().removeIf(playerInfo -> vanishManager.isVanished(playerInfo.id()));

        // 2. Count ALL online vanished players
        int vanishedCount = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (vanishManager.isVanished(online.getUniqueId())) {
                vanishedCount++;
            }
        }

        // 3. Subtract the vanished count from the total count
        event.setNumPlayers(Math.max(0, event.getNumPlayers() - vanishedCount));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();
        VanishManager vanishManager = plugin.getVanishManager();

        // 1. If the joining player is already vanished, hide them from everyone currently online
        if (vanishManager.isVanished(joiningPlayer.getUniqueId())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(joiningPlayer) && !online.hasPermission("niso.vanish.see")) {
                    online.hidePlayer(plugin, joiningPlayer);
                }
            }
            joiningPlayer.sendMessage(plugin.prefixMessage(Component.text("You joined while vanished!").color(NamedTextColor.YELLOW)));
        }

        // 2. Hide all existing vanished players from the joining player
        if (!joiningPlayer.hasPermission("niso.vanish.see")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (vanishManager.isVanished(online.getUniqueId())) {
                    joiningPlayer.hidePlayer(plugin, online);
                }
            }
        }
    }
}