package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

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

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        validateConfigKey(config, "auto-update", true, Type.BOOLEAN);
        validateConfigKey(config, "update-notifications", true, Type.BOOLEAN);
        validateConfigKey(config, "debug", false, Type.BOOLEAN);
        validateConfigKey(config, "message-prefix", "<dark_gray>[<gradient:#FF5CCC:#743296>Niso<dark_gray>] <reset>", Type.STRING);
        validateConfigKey(config, "tablist.enabled", false, Type.BOOLEAN);
        validateConfigKey(config, "tablist.update-interval", 100, Type.INT);
        validateConfigKey(config, "tablist.header", "<dark_gray><strikethrough> ]                                                                  [ <reset>\n<gray>\n<gradient:#FF5CCC:#743296>mc.niso.moe\n<gray>", Type.STRING);
        validateConfigKey(config, "tablist.footer", "<gray>\n<gray>TPS: <green>%server_tps_15% <dark_gray>| <gray>Your Ping: <green>%player_ping%ms\n<gray>Players: <green>%server_online% <dark_gray>| <gray>Uptime: <green>%server_uptime%\n<gray>\n<dark_gray><strikethrough> ]                                                                  [ <reset>", Type.STRING);
        validateConfigKey(config, "welcome-message.enabled", false, Type.BOOLEAN);
        validateConfigKey(config, "welcome-message.personal-message.enabled", true, Type.BOOLEAN);
        validateConfigKey(config, "welcome-message.personal-message.message", "<gray>Welcome to the server, <green>%player_name%<gray>!", Type.STRING);
        validateConfigKey(config, "welcome-message.broadcast-message.enabled", true, Type.BOOLEAN);
        validateConfigKey(config, "welcome-message.broadcast-message.message", "<dark_gray>[<green>+<dark_gray>] <gray>%player_name%", Type.STRING);
        validateConfigKey(config, "leave-message.enabled", false, Type.BOOLEAN);
        validateConfigKey(config, "leave-message.broadcast-message.enabled", true, Type.BOOLEAN);
        validateConfigKey(config, "leave-message.broadcast-message.message", "<dark_gray>[<red>-<dark_gray>] <gray>%player_name%", Type.STRING);
        validateConfigKey(config, "chat-format.enabled", false, Type.BOOLEAN);
        validateConfigKey(config, "chat-format.format", "<gray>%player_name% <dark_gray>» <reset>%message%", Type.STRING);
        validateConfigKey(config, "server-motd.enabled", false, Type.BOOLEAN);
        validateConfigKey(config, "server-motd.first-line", "<gray>                     <rainbow><b>mc.niso.moe</b></rainbow>", Type.STRING);
        validateConfigKey(config, "server-motd.second-line", "<gray>                         by Nikki</gray>", Type.STRING);
        validateConfigKey(config, "database.host", "localhost", Type.STRING);
        validateConfigKey(config, "database.port", 5432, Type.INT);
        validateConfigKey(config, "database.database", "minecraft", Type.STRING);
        validateConfigKey(config, "database.username", "username", Type.STRING);
        validateConfigKey(config, "database.password", "super_secret", Type.STRING);
        validateConfigKey(config, "database.pool-size", 10, Type.INT);

        try {
            config.save(configFile);
            plugin.reloadConfig();
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
    private static void validateConfigKey(YamlConfiguration config, String key, Object defaultValue, Type type) {
        boolean isValid = switch (type) {
            case Type.BOOLEAN -> config.isBoolean(key);
            case Type.INT -> config.isInt(key);
            case Type.STRING -> config.isString(key);
        };

        if (!isValid) {
            plugin.getLogger().warning(key + " key not found or invalid in config, adding default value...");
            config.set(key, defaultValue);
        }
    }

    /**
     * Enum for configuration value types.
     */
    enum Type {
        BOOLEAN, INT, STRING
    }
}