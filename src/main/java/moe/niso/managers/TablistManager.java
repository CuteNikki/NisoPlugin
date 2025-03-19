package moe.niso.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TablistManager {
    private static final NisoPlugin plugin = NisoPlugin.getInstance();

    private final BukkitTask task;

    public TablistManager() {
        final ConfigurationSection tablistConfig = plugin.getConfig().getConfigurationSection("tablist");

        if (tablistConfig == null || !tablistConfig.getBoolean("enabled")) {
            task = null;
            return;
        }

        int updateInterval = tablistConfig.getInt("update-interval");

        if (updateInterval < 20 || updateInterval > 1000) {
            updateInterval = 100;
        }


        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateTablist(null);
            }
        }.runTaskTimerAsynchronously(plugin, 0, updateInterval);
    }

    public static void updateTablist(Player player) {
        final ConfigurationSection tablistConfig = plugin.getConfig().getConfigurationSection("tablist");

        if (tablistConfig == null) {
            return;
        }

        final boolean isEnabled = tablistConfig.getBoolean("enabled");
        final String header = tablistConfig.getString("header");
        final String footer = tablistConfig.getString("footer");

        if (!isEnabled || header == null || footer == null) {
            return;
        }

        if (player != null) {
            final Component headerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, header));
            final Component footerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, footer));
            player.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
        } else {
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                final Component headerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(onlinePlayer, header));
                final Component footerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(onlinePlayer, footer));

                onlinePlayer.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
            }
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
