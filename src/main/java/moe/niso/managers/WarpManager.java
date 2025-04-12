package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WarpManager {
    private final static NisoPlugin plugin = NisoPlugin.getInstance();

    private static List<String> warpNamesCache = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(5); // 5 minutes

    /**
     * Cleans up expired warp cache entries.
     * This method is called periodically to ensure that the cache does not grow indefinitely.
     */
    public static void startWarpCacheCleanup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanExpiredWarpCache();
            }
        }.runTaskTimer(plugin, 0L, 1200L * 5); // Run every 5 minutes
    }

    /**
     * Cleans up expired warp cache entries.
     * This method is called periodically to ensure that the cache does not grow indefinitely.
     */
    private static void cleanExpiredWarpCache() {
        long currentTime = System.currentTimeMillis();
        if (warpNamesCache != null && (currentTime - cacheTimestamp) > CACHE_EXPIRATION_TIME) {
            invalidateWarpCache();
        }
    }

    /**
     * Invalidates the warp cache.
     * This method should be called when the warp data is modified to ensure that the cache is up-to-date.
     */
    public static void invalidateWarpCache() {
        warpNamesCache = null; // Invalidate the cache
        cacheTimestamp = 0;
    }

    public static void updateWarpCache(List<String> homes) {
        long currentTime = System.currentTimeMillis();
        warpNamesCache = homes;
        cacheTimestamp = currentTime;
    }

    /**
     * Sets a warp in the database.
     * If the warp already exists, it will be updated.
     *
     * @param player   The player setting the warp
     * @param warpName The name of the warp
     * @return True if the warp was set successfully, false otherwise
     */
    public static boolean setWarp(Player player, String warpName) {
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        // SQL query to insert the warp if it doesn't exist already
        String sql = "INSERT INTO warps (creator_uuid, warp_name, world, x, y, z, pitch, yaw, created_at) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " + "ON CONFLICT (warp_name) DO UPDATE SET " + "world = excluded.world, x = excluded.x, y = excluded.y, z = excluded.z, " + "pitch = excluded.pitch, yaw = excluded.yaw, created_at = excluded.created_at;";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                ps.setObject(1, player.getUniqueId());
                ps.setString(2, warpName);
                ps.setString(3, worldName);
                ps.setDouble(4, x);
                ps.setDouble(5, y);
                ps.setDouble(6, z);
                ps.setFloat(7, pitch);
                ps.setFloat(8, yaw);

                ps.executeUpdate();
                connection.commit(); // Commit the transaction if all goes well

                invalidateWarpCache(); // Invalidate the cache after setting the warp

                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("Warp set by player " + player.getName() + " (" + player.getUniqueId() + ") at world: " + worldName + " x: " + x + " y: " + y + " z:" + z + " pitch: " + pitch + " yaw: " + yaw);
                }
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction in case of an error
                plugin.getLogger().warning("Error setting warp for player: " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error setting warp for player: " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
            return false;
        }
    }

    /**
     * Retrieves a warp location from the database.
     *
     * @param warpName The name of the warp
     * @return The warp location, or null if the warp was not found
     */
    public static Location getWarp(String warpName) {
        // SQL query to retrieve the warp location
        String sql = "SELECT world, x, y, z, pitch, yaw FROM warps WHERE warp_name = ?;";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, warpName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Location(plugin.getServer().getWorld(rs.getString("world")), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error getting warp location: " + e.getMessage());
        }

        return null; // Warp not found
    }

    /**
     * Deletes a warp from the database.
     *
     * @param warpName The name of the warp to delete
     * @return True if the warp was deleted successfully, false otherwise
     */
    public static boolean deleteWarp(String warpName) {
        // SQL query to delete the warp
        String sql = "DELETE FROM warps WHERE warp_name = ?;";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, warpName);

                ps.executeUpdate();
                connection.commit(); // Commit the transaction if all goes well

                invalidateWarpCache(); // Invalidate the cache after deleting the warp

                if (plugin.getConfig().getBoolean("debug")) {
                    plugin.getLogger().info("Warp deleted: " + warpName);
                }
                return true;
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction in case of an error
                plugin.getLogger().warning("Error deleting warp: " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error deleting warp: " + e.getMessage() + " [SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() + "]");
            return false;
        }
    }

    /**
     * Retrieves a list of all warp names from the cache or database.
     *
     * @return A set of warp names
     */
    public static List<String> getWarpNames() {
        return getWarpNames(false);
    }

    /**
     * Retrieves a list of all warp names from the cache or database.
     *
     * @return A set of warp names
     */
    public static List<String> getWarpNames(boolean forceRefresh) {
        long currentTime = System.currentTimeMillis();

        // If the cache is still valid, return the cached warp names
        if (!forceRefresh && warpNamesCache != null && (currentTime - cacheTimestamp) < CACHE_EXPIRATION_TIME) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Returning " + warpNamesCache.size() + " cached warp names: " + warpNamesCache);
            }

            return warpNamesCache;
        }

        // SQL query to retrieve all warp names
        String sql = "SELECT warp_name FROM warps;";
        List<String> warpNames = new ArrayList<>();

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                warpNames.add(rs.getString("warp_name"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error getting warp names: " + e.getMessage());
        }

        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("Retrieved " + warpNames.size() + " warp names: " + warpNames);
        }

        updateWarpCache(warpNames);

        return warpNames;
    }

    /**
     * Checks if a warp name is valid.
     *
     * @param warpName The warp name to check
     * @return True if the warp name is valid, false otherwise
     */
    public static boolean isValidWarpName(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Warp name is null or empty.");
            }
            return false;
        }
        if (warpName.length() > 20) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Warp name is too long.");
            }
            return false;
        }
        if (!warpName.matches("^[a-zA-Z0-9_]+$")) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Warp name contains invalid characters.");
            }
            return false;
        }

        return true;
    }
}
