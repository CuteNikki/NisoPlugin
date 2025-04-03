package moe.niso.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @EventHandler
    public void onMessage(AsyncChatEvent event) {
        final ConfigurationSection chatConfig = plugin.getConfig().getConfigurationSection("chat-format");

        if (chatConfig == null || !chatConfig.getBoolean("enabled")) {
            return;
        }

        final String format = chatConfig.getString("format");

        if (format == null) {
            return;
        }

        final String finalFormat = PlaceholderAPI.setPlaceholders(event.getPlayer(), format);

        event.renderer((source, sourceDisplayName, message, viewer) -> MiniMessage.miniMessage()
                .deserialize(finalFormat)
                .replaceText(builder -> builder.matchLiteral("%player_name%").replacement(sourceDisplayName))
                .replaceText(builder -> builder.matchLiteral("%message%").replacement(message)));
    }
}
