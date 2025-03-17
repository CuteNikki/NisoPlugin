package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigManager {
    private static final NisoPlugin plugin = NisoPlugin.getInstance();

    /**
     * Checks the default configuration file and creates it if it doesn't exist.
     */
    public static void checkDefaultConfig() {

        final File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.getLogger().info("Config file not found, creating default config...");
            plugin.saveDefaultConfig();
            return;
        } else {
            plugin.getLogger().info("Config file found, checking for missing values...");
        }

        final FileConfiguration config = plugin.getConfig();

        validateConfigKey(config, "auto-update", true, "boolean");
        validateConfigKey(config, "update-notifications", true, "boolean");
        validateConfigKey(config, "debug", false, "boolean");
        validateConfigKey(config, "message-prefix", "<dark_gray>[<gradient:#FF5CCC:#743296>Niso<dark_gray>] <reset>", "string");
        validateConfigKey(config, "tablist.enabled", false, "boolean");
        validateConfigKey(config, "tablist.update-interval", 100, "int");
        validateConfigKey(config, "tablist.header", "<dark_gray><strikethrough> ]                                                                  [ <reset>\n<gray>\n<gradient:#FF5CCC:#743296>mc.niso.moe\n<gray>", "string");
        validateConfigKey(config, "tablist.footer", "<gray>\n<gray>TPS: <green>%server_tps_15% <dark_gray>| <gray>Your Ping: <green>%player_ping%ms\n<gray>Players: <green>%server_online% <dark_gray>| <gray>Uptime: <green>%server_uptime%\n<gray>\n<dark_gray><strikethrough> ]                                                                  [ <reset>", "string");
        validateConfigKey(config, "welcome-message.enabled", false, "boolean");
        validateConfigKey(config, "welcome-message.personal-message.enabled", true, "boolean");
        validateConfigKey(config, "welcome-message.personal-message.message", "<gray>Welcome to the server, <green>%player_name%<gray>!", "string");
        validateConfigKey(config, "welcome-message.broadcast-message.enabled", true, "boolean");
        validateConfigKey(config, "welcome-message.broadcast-message.message", "<dark_gray>[<green>+<dark_gray>] <gray>%player_name%", "string");
        validateConfigKey(config, "leave-message.enabled", false, "boolean");
        validateConfigKey(config, "leave-message.broadcast-message.enabled", true, "boolean");
        validateConfigKey(config, "leave-message.broadcast-message.message", "<dark_gray>[<red>-<dark_gray>] <gray>%player_name%", "string");
        validateConfigKey(config, "chat-format.enabled", false, "boolean");
        validateConfigKey(config, "chat-format.format", "<gray>%player_name% <dark_gray>» <reset>%message%", "string");
        validateConfigKey(config, "server-motd.enabled", false, "boolean");
        validateConfigKey(config, "server-motd.first-line", "<gray>                     <rainbow><b>mc.niso.moe</b></rainbow>", "string");
        validateConfigKey(config, "server-motd.second-line", "<gray>                         by Nikki</gray>", "string");
        validateConfigKey(config, "database.host", "localhost", "string");
        validateConfigKey(config, "database.port", 5432, "int");
        validateConfigKey(config, "database.database", "minecraft", "string");
        validateConfigKey(config, "database.username", "username", "string");
        validateConfigKey(config, "database.password", "super_secret", "string");
        validateConfigKey(config, "database.pool-size", 10, "int");

        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save default config: " + e.getMessage());
            plugin.getLogger().severe("Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Validates a key in the configuration file and adds a default value if it's missing or invalid.
     *
     * @param config       Configuration file
     * @param key          Key to validate
     * @param defaultValue Default value to add if the key is missing or invalid
     * @param type         Type of the value (boolean, int, string)
     */
    private static void validateConfigKey(FileConfiguration config, String key, Object defaultValue, String type) {
        boolean isValid = switch (type) {
            case "boolean" -> config.isBoolean(key);
            case "int" -> config.isInt(key);
            case "string" -> config.isString(key);
            default -> false;
        };

        if (!isValid) {
            plugin.getLogger().warning(key + " key not found or invalid in config, adding default value...");
            config.set(key, defaultValue);
        }
    }
}
