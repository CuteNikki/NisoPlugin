package moe.niso.managers;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HomeManager {
    private final static NisoPlugin plugin = NisoPlugin.getInstance();

    private final static ConcurrentHashMap<UUID, List<String>> homeCache = new ConcurrentHashMap<>();
    private final static long CACHE_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(5); // Cache expiration time of 5 minutes
    private final static ConcurrentHashMap<UUID, Long> cacheTimestamps = new ConcurrentHashMap<>();

    /**
     * Starts a periodic task to clean up expired cache entries.
     * This method should be called when the plugin is enabled.
     */
    public static void startHomeCacheCleanup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanExpiredCache();
            }
        }.runTaskTimer(plugin, 0L, 1200L * 5); // Runs every 5 minutes (5 * 1200 ticks)
    }

    /**
     * Invalidates the home cache for a player.
     *
     * @param uuid The UUID of the player
     */
    public static void invalidateHomeCache(UUID uuid) {
        homeCache.remove(uuid);
        cacheTimestamps.remove(uuid);
    }

    /**
     * Updates the home cache for a player.
     *
     * @param uuid  The UUID of the player
     * @param homes The list of home names
     */
    private static void updateHomeCache(UUID uuid, List<String> homes) {
        homeCache.put(uuid, homes);
        cacheTimestamps.put(uuid, System.currentTimeMillis());
    }

    /**
     * Cleans up expired cache entries.
     * This method should be called periodically to remove expired cache entries.
     */
    private static void cleanExpiredCache() {
        long now = System.currentTimeMillis();

        for (UUID uuid : cacheTimestamps.keySet()) {
            if (now - cacheTimestamps.get(uuid) >= CACHE_EXPIRATION_TIME) {
                homeCache.remove(uuid);
                cacheTimestamps.remove(uuid);
            }
        }
    }

    /**
     * Gets the maximum number of homes a player can have.
     *
     * @param player The player to check
     * @return The maximum number of homes the player can have
     */
    public static int getHomeLimit(Player player) {
        int maxHomes = 1; // Default limit

        for (int index = 2; index <= 50; index++) {
            if (player.hasPermission("niso.home.limit-" + index)) {
                maxHomes = index;
            }
        }

        return maxHomes;
    }

    /**
     * Sets a home for a player in the database.
     * If the home already exists, it will be updated.
     *
     * @param player   The player to set the home for
     * @param homeName The name of the home
     * @return True if the home was set successfully, false otherwise
     */
    public static boolean setHome(Player player, String homeName) {
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        // SQL query to insert the home if it doesn't exist already
        String sql = "INSERT INTO homes (creator_uuid, home_name, world, x, y, z, pitch, yaw, created_at) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " + "ON CONFLICT (creator_uuid, home_name) DO UPDATE SET " + "world = excluded.world, x = excluded.x, y = excluded.y, z = excluded.z, " + "pitch = excluded.pitch, yaw = excluded.yaw, created_at = excluded.created_at;";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setObject(1, player.getUniqueId());
                ps.setString(2, homeName);
                ps.setString(3, worldName);
                ps.setDouble(4, x);
                ps.setDouble(5, y);
                ps.setDouble(6, z);
                ps.setFloat(7, pitch);
                ps.setFloat(8, yaw);

                ps.executeUpdate();
                connection.commit(); // Commit the transaction if all goes well

                invalidateHomeCache(player.getUniqueId()); // Invalidate the cache after setting the home

                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("Home set by player " + player.getName() + " (" + player.getUniqueId() + ") at world: " + worldName + " x: " + x + " y: " + y + " z:" + z + " pitch: " + pitch + " yaw: " + yaw);
                }
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction in case of an error
                plugin.getLogger().warning("Error setting home for player " + player.getName() + ": " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error setting home for player " + player.getName() + " (" + player.getUniqueId() + "): at world: " + worldName + " x: " + x + " y: " + y + " z:" + z + " pitch: " + pitch + " yaw: " + yaw + " error: " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
            return false;
        }
    }

    /**
     * Retrieves a home location from the database for a player.
     *
     * @param player   The player to get the home for
     * @param homeName The name of the home to retrieve
     * @return The home location, or null if not found
     */
    public static Location getHome(Player player, String homeName) {
        // SQL query to retrieve the home location
        String sql = "SELECT world, x, y, z, pitch, yaw FROM homes WHERE creator_uuid = ? AND home_name = ?";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, player.getUniqueId());
            ps.setString(2, homeName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String worldName = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float pitch = rs.getFloat("pitch");
                    float yaw = rs.getFloat("yaw");

                    return new Location(plugin.getServer().getWorld(worldName), x, y, z, yaw, pitch);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error retrieving home for player " + player.getName() + ": " + e.getMessage());
            player.sendMessage(plugin.prefixMessage(Component.text("Database error occurred while retrieving your home. Please try again later.").color(NamedTextColor.RED)));
        }

        return null;  // Home not found
    }


    /**
     * Deletes a home from the database.
     *
     * @param player   The player to delete the home for
     * @param homeName The name of the home to delete
     * @return True if the home was deleted, false otherwise
     */
    public static boolean deleteHome(Player player, String homeName) {
        String sql = "DELETE FROM homes WHERE creator_uuid = ? AND home_name = ?";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setObject(1, player.getUniqueId());
                ps.setString(2, homeName);

                int rowsAffected = ps.executeUpdate();
                connection.commit(); // Commit the transaction if all goes well

                // Invalidate the cache for this player
                invalidateHomeCache(player.getUniqueId());

                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("Home deleted by player " + player.getName() + " (" + player.getUniqueId() + ") with name: " + homeName);
                }

                return rowsAffected > 0; // True if rows were deleted
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction in case of an error
                plugin.getLogger().warning("Error deleting home for player " + player.getName() + ": " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error deleting home from database: " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
            return false;
        }
    }

    /**
     * Retrieves a list of home names for a player.
     *
     * @param player The player to get homes for
     * @return A list of home names
     */
    public static List<String> getHomeNames(Player player) {
        return getHomeNames(player, false);  // Default behavior with forceRefresh set to false
    }

    /**
     * Retrieves a list of home names for a player.
     *
     * @param player       The player to get homes for
     * @param forceRefresh Whether to bypass the cache and refresh it
     * @return A list of home names
     */
    public static List<String> getHomeNames(Player player, boolean forceRefresh) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // If forceRefresh is true, bypass the cache and fetch the data
        if (forceRefresh || !homeCache.containsKey(uuid) || (now - cacheTimestamps.getOrDefault(uuid, 0L) >= CACHE_EXPIRATION_TIME)) {
            List<String> homes = new ArrayList<>();
            String sql = "SELECT home_name FROM homes WHERE creator_uuid = ?";

            try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setObject(1, player.getUniqueId());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        homes.add(rs.getString("home_name"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Error getting homes from database for player " + player.getName() + ": " + e.getMessage());
                player.sendMessage(plugin.prefixMessage(Component.text("An error occurred while retrieving your homes. Please try again later.").color(NamedTextColor.RED)));
            }

            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Retrieved " + homes.size() + " homes for player " + player.getName() + " (" + player.getUniqueId() + "): " + homes);
            }

            // Store the homes in the cache
            updateHomeCache(player.getUniqueId(), homes);

            return homes;
        } else {
            List<String> cachedHomes = homeCache.get(uuid);

            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Returning " + cachedHomes.size() + " cached homes for player " + player.getName() + " (" + player.getUniqueId() + "): " + cachedHomes);
            }

            // Return cached homes if available
            return cachedHomes;
        }
    }

    /**
     * Checks if a home name is valid.
     *
     * @param homeName The name of the home to check
     * @return True if the home name is valid, false otherwise
     */
    public static boolean isValidHomeName(String homeName) {
        if (homeName == null || homeName.isEmpty()) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Home name is empty or null");
            }
            return false;
        }

        if (homeName.length() > 20) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Home name is too long");
            }
            return false;
        }

        if (!homeName.matches("^[a-zA-Z0-9_]+$")) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Home name contains invalid characters");
            }
            return false;
        }

        return true;
    }

}
