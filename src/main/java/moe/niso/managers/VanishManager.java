package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    private final NisoPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public VanishManager(NisoPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && !online.hasPermission("niso.vanish.see")) {
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(plugin, player);
            }
        }
        saveVanished(player.getUniqueId(), vanished);
    }

    public boolean toggleVanish(Player player) {
        boolean newState = !isVanished(player.getUniqueId());
        setVanished(player, newState);
        return newState;
    }

    /**
     * Stores a change in the database right away, off the main thread, so it lasts through restarts and crashes.
     */
    private void saveVanished(UUID uuid, boolean vanished) {
        final String sql = vanished
                ? "INSERT INTO vanished_players (player_uuid) VALUES (?) ON CONFLICT (player_uuid) DO NOTHING;"
                : "DELETE FROM vanished_players WHERE player_uuid = ?;";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setObject(1, uuid);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Error saving vanish state: " + e.getMessage());
            }
        });
    }

    /**
     * Loads the vanished players from the database, after moving over any that are still in config.yml.
     */
    public void loadVanishedPlayers() {
        moveVanishedPlayersFromConfig();

        final String sql = "SELECT player_uuid FROM vanished_players;";
        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vanishedPlayers.add(rs.getObject("player_uuid", UUID.class));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading vanished players: " + e.getMessage());
        }
    }

    /**
     * Vanished players used to be kept in config.yml, which meant saving the whole config on shutdown and overwriting
     * changes made to it while the server was running. Moves them into the database and removes them from the file.
     */
    private void moveVanishedPlayersFromConfig() {
        final File configFile = new File(plugin.getDataFolder(), "config.yml");
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception e) {
            // A config that can't be read is reported by the ConfigManager, and left as it is
            return;
        }
        if (!config.contains("vanished-players")) return;

        final List<String> list = config.getStringList("vanished-players");
        final String sql = "INSERT INTO vanished_players (player_uuid) VALUES (?) ON CONFLICT (player_uuid) DO NOTHING;";
        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int moved = 0;
            for (String uuid : list) {
                try {
                    ps.setObject(1, UUID.fromString(uuid));
                    ps.addBatch();
                    moved++;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Skipping vanished player with an invalid UUID: " + uuid);
                }
            }
            ps.executeBatch();
            plugin.getLogger().info("Moved " + moved + " vanished player(s) from config.yml to the database.");
        } catch (SQLException e) {
            // They stay in config.yml, so the next start tries again
            plugin.getLogger().warning("Error moving vanished players to the database: " + e.getMessage());
            return;
        }

        config.set("vanished-players", null);
        try {
            config.save(configFile);
            plugin.reloadConfig();
        } catch (Exception e) {
            plugin.getLogger().warning("Vanished players are in the database now, but couldn't be removed from config.yml: "
                    + e.getMessage());
        }
    }
}
