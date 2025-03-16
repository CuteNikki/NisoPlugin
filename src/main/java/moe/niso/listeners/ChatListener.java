package moe.niso.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    @EventHandler
    public void onMessage(AsyncChatEvent event) {
        event.renderer((source, sourceDisplayName, message, viewer) -> MiniMessage.miniMessage().deserialize(NisoPlugin.getInstance().getChatFormat()).replaceText(builder -> builder.matchLiteral("%player_name%").replacement(sourceDisplayName)).replaceText(builder -> builder.matchLiteral("%message%").replacement(message)));
    }
}
