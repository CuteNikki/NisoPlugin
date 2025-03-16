package moe.niso.listeners;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MotdListener implements Listener {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        String motdText = plugin.getServerMotd();
        Component motd =
                MiniMessage.miniMessage().deserialize(Arrays.stream(motdText.split("\n")).map(line -> StringUtils.center(line, 70)).collect(Collectors.joining("\n")));

        event.motd(motd);
        event.setMaxPlayers(plugin.getServer().getMaxPlayers());
        event.setServerIcon(plugin.getServer().getServerIcon());
    }
}
