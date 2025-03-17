package moe.niso.listeners;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MotdListener implements Listener {
    private final NisoPlugin plugin = NisoPlugin.getInstance();

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        final ConfigurationSection motdConfig = plugin.getConfig().getConfigurationSection("server-motd");

        if (motdConfig == null || !motdConfig.getBoolean("enabled")) {
            return;
        }

        final String firstLine = motdConfig.getString("first-line");
        final String secondLine = motdConfig.getString("second-line");

        if (firstLine == null || secondLine == null) {
            return;
        }

        final Component firstLineComponent = MiniMessage.miniMessage().deserialize(firstLine);
        final Component secondLineComponent = MiniMessage.miniMessage().deserialize(secondLine);

        event.motd(firstLineComponent.append(Component.newline()).append(secondLineComponent));

        event.setMaxPlayers(plugin.getServer().getMaxPlayers());
        event.setServerIcon(plugin.getServer().getServerIcon());
    }
}
