package moe.niso.managers;

import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HomeManager {
    private final static NisoPlugin plugin = NisoPlugin.getInstance();

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

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, player.getUniqueId());
            ps.setString(2, homeName);
            ps.setString(3, worldName);
            ps.setDouble(4, x);
            ps.setDouble(5, y);
            ps.setDouble(6, z);
            ps.setFloat(7, pitch);
            ps.setFloat(8, yaw);

            ps.executeUpdate();

            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Home set by player " + player.getName() + " (" + player.getUniqueId() + ") at world: " + worldName + " x: " + x + " y: " + y + " z:" + z + " pitch: " + pitch + " yaw: " + yaw);
            }

            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("Error setting home for player " + player.getName() + " (" + player.getUniqueId() + "): at world: " + worldName + " x: " + x + " y: " + y + " z:" + z + " pitch: " + pitch + " yaw: " + yaw + " error: " + e.getMessage());
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

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, player.getUniqueId());
            ps.setString(2, homeName);

            int rowsAffected = ps.executeUpdate();

            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Home deleted by player " + player.getName() + " (" + player.getUniqueId() + ") with name: " + homeName);
            }

            return rowsAffected > 0; // True if rows were deleted

        } catch (SQLException e) {
            plugin.getLogger().warning("Error deleting home from database: " + e.getMessage());
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
        String sql = "SELECT home_name FROM homes WHERE creator_uuid = ?";
        List<String> homes = new ArrayList<>();

        try (Connection connection = plugin.getDatabaseManager().getDataSource().getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, player.getUniqueId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    homes.add(rs.getString("home_name"));
                }
            }

            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Retrieved " + homes.size() + " homes for player " + player.getName() + " (" + player.getUniqueId() + "): " + homes);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error getting homes from database for player " + player.getName() + ": " + e.getMessage());
            player.sendMessage(plugin.prefixMessage(Component.text("An error occurred while retrieving your homes. Please try again later.").color(NamedTextColor.RED)));
        }

        return homes;
    }

    /**
     * Checks if a home name is valid.
     *
     * @param homeName The name of the home to check
     * @return True if the home name is valid, false otherwise
     */
    public static boolean isValidHomeName(String homeName) {
        if (homeName == null || homeName.isEmpty()) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Home name is empty or null");
            }
            return false;
        }

        if (homeName.length() > 20) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Home name is too long");
            }
            return false;
        }

        if (!homeName.matches("^[a-zA-Z0-9_]+$")) {
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("Home name contains invalid characters");
            }
            return false;
        }

        return true;
    }

}
