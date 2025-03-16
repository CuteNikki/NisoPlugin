package moe.niso.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        String leaveText = PlaceholderAPI.setPlaceholders(player, NisoPlugin.getInstance().getLeaveMessage());
        final Component message = MiniMessage.miniMessage().deserialize(leaveText);

        event.quitMessage(message);
    }
}