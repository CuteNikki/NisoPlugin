package moe.niso.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        String welcomeText = PlaceholderAPI.setPlaceholders(player, NisoPlugin.getInstance().getWelcomeMessage());
        final Component welcomeMessage = MiniMessage.miniMessage().deserialize(welcomeText);

        event.joinMessage(welcomeMessage);
    }
}