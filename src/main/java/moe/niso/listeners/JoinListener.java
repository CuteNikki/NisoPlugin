package moe.niso.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import moe.niso.managers.TablistManager;
import moe.niso.managers.VersionManager;
import moe.niso.web.ResourcePackServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class JoinListener implements Listener {
    private final NisoPlugin plugin = NisoPlugin.getInstance();
    private final ResourcePackServer packServer;

    public JoinListener(ResourcePackServer packServer) {
        this.packServer = packServer;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        boolean resourcePackEnabled = plugin.getConfig().getBoolean("resource-pack.enabled");
        if (resourcePackEnabled) {
            String resourcePackFileName = plugin.getConfig().getString("resource-pack.file-name", "resource_pack.zip");
            int serverPort = plugin.getConfig().getInt("resource-pack.server-port", 8080);
            String serverIp = plugin.getConfig().getString("resource-pack.server-ip", "localhost");

            String resourcePackURL = "http://" + serverIp + ":" + serverPort + "/" + resourcePackFileName;
            boolean forceDownload = plugin.getConfig().getBoolean("resource-pack.force-download", false);
            String promptMessage = plugin.getConfig().getString("resource-pack.prompt-message", "<red>Resource Pack Download");

            // Use the cached hash instead of recalculating it
            byte[] hash = packServer.getCachedHash();

            player.setResourcePack(resourcePackURL, hash, MiniMessage.miniMessage().deserialize(promptMessage), forceDownload);
        }

        // If the joining player is vanished, hide them from other players
        if (player.hasMetadata("vanished")) {
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("niso.vanish.see")) {
                    player.hidePlayer(plugin, onlinePlayer);
                } else {
                    player.showPlayer(plugin, onlinePlayer);
                }
            }
        }

        // If an online player is vanished, hide them from the joining player
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasMetadata("vanished")) {
                if (player.hasPermission("niso.vanish.see")) {
                    player.showPlayer(plugin, onlinePlayer);
                } else {
                    player.hidePlayer(plugin, onlinePlayer);
                }
            }
        }

        // Sending welcome messages

        final ConfigurationSection welcomeConfig = plugin.getConfig().getConfigurationSection("welcome-message");
        if (welcomeConfig == null || !welcomeConfig.getBoolean("enabled")) {
            return;
        }

        // Personal message
        final ConfigurationSection personalMessageConfig = welcomeConfig.getConfigurationSection("personal-message");
        if (personalMessageConfig != null && personalMessageConfig.getBoolean("enabled")) {
            final String personalMessage = personalMessageConfig.getString("message");
            if (personalMessage != null) {
                final Component personalMessageComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, personalMessage));
                player.sendMessage(personalMessageComponent);
            }

        }

        // Broadcast message
        final ConfigurationSection broadcastMessageConfig = welcomeConfig.getConfigurationSection("broadcast-message");
        if (broadcastMessageConfig != null && broadcastMessageConfig.getBoolean("enabled")) {
            final String broadcastMessage = broadcastMessageConfig.getString("message");
            if (broadcastMessage != null) {
                final Component broadcastMessageComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, broadcastMessage));
                event.joinMessage(broadcastMessageComponent);
            }
        }

        // Check if player is op, an update is available and then notify the player
        if (player.isOp() && plugin.getConfig().getBoolean("update-notifications")) {
            final String currentVersion = VersionManager.getCurrentVersion();
            final String newestVersion = VersionManager.getNewestVersion();
            final boolean updateAvailable = VersionManager.isNewerVersion(currentVersion, newestVersion);

            if (updateAvailable) {
                final Component updateMessageComponent = Component.text("An update for the plugin is available!").color(NamedTextColor.GREEN).append(Component.newline()).append(Component.text("Current version: " + currentVersion).color(NamedTextColor.RED)).append(Component.newline()).append(Component.text("Newest version: " + newestVersion).color(NamedTextColor.AQUA));
                player.sendMessage(plugin.prefixMessage(updateMessageComponent));
            }
        }

        // Updating the tablist
        final ConfigurationSection tablistConfig = plugin.getConfig().getConfigurationSection("tablist");
        if (tablistConfig != null && tablistConfig.getBoolean("enabled")) {
            TablistManager.updateTablist(player);
            TablistManager.setAllPlayerNames();
        }
    }
}