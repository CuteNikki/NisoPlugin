package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WarpManager {
    private final static NisoPlugin plugin = NisoPlugin.getInstance();

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

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, player.getUniqueId());
            ps.setString(2, warpName);
            ps.setString(3, worldName);
            ps.setDouble(4, x);
            ps.setDouble(5, y);
            ps.setDouble(6, z);
            ps.setFloat(7, pitch);
            ps.setFloat(8, yaw);

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error setting warp for player: " + e.getMessage());
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

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, warpName);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error deleting warp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a list of all warp names from the database.
     *
     * @return A set of warp names
     */
    public static List<String> getWarpNames() {
        // SQL query to retrieve all warp names
        String sql = "SELECT warp_name FROM warps;";

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<String> warpNames = new ArrayList<>();

            while (rs.next()) {
                warpNames.add(rs.getString("warp_name"));
            }

            return warpNames;

        } catch (SQLException e) {
            plugin.getLogger().warning("Error getting warp names: " + e.getMessage());
        }

        return List.of(); // No warps found
    }

    /**
     * Checks if a warp name is valid.
     *
     * @param warpName The warp name to check
     * @return True if the warp name is valid, false otherwise
     */
    public static boolean isValidWarpName(String warpName) {
        if (warpName == null || warpName.isEmpty()) {
            return false;
        }
        if (warpName.length() > 20) {
            return false;
        }
        // Disallow special characters or other restrictions
        return warpName.matches("^[a-zA-Z0-9_]+$");
    }
}
