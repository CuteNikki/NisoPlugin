package moe.niso.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        // Sending leave messages

        final ConfigurationSection leaveConfig = plugin.getConfig().getConfigurationSection("leave-message");

        if (leaveConfig == null || !leaveConfig.getBoolean("enabled")) {
            return;
        }

        // Broadcast message
        final ConfigurationSection broadcastMessageConfig = leaveConfig.getConfigurationSection("broadcast-message");
        if (broadcastMessageConfig != null && broadcastMessageConfig.getBoolean("enabled")) {
            final String broadcastMessage = broadcastMessageConfig.getString("message");
            if (broadcastMessage != null) {
                final Component broadcastMessageComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, broadcastMessage));
                event.quitMessage(broadcastMessageComponent);
            }
        }
    }
}