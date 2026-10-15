package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static final NisoPlugin plugin = NisoPlugin.getInstance();

    /**
     * Allowed ranges for number values, as {min, max}. Values outside them are replaced with the default.
     */
    private static final Map<String, int[]> RANGES = Map.of(
            "tablist.update-interval", new int[]{20, 1000},
            "database.port", new int[]{1, 65535},
            "database.pool-size", new int[]{1, 50},
            "database.minimum-idle", new int[]{0, 50},
            "resource-pack.server-port", new int[]{1, 65535}
    );

    /**
     * Creates the config file if it doesn't exist. Otherwise, checks it against the default config bundled with the
     * plugin: missing values are added, values of the wrong type or outside their range are replaced with the default,
     * and the comments on its settings are brought up to date. Values the default config doesn't have are left alone
     * with their comments.
     */
    public static void checkDefaultConfig() {
        final File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.getLogger().info("Config file not found, creating default config...");
            plugin.saveDefaultConfig();
            return;
        }
        plugin.getLogger().info("Config file found, checking for missing or invalid values...");

        final YamlConfiguration defaults = loadDefaults();
        if (defaults == null) return;
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception e) {
            // Checking it would mean overwriting every setting with its default
            plugin.getLogger().severe("config.yml couldn't be read, so it's left as it is. Fix it and restart: "
                    + e.getMessage());
            return;
        }

        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key)) continue;

            final Object defaultValue = defaults.get(key);
            final String problem = findProblem(config, key, defaultValue);
            if (problem == null) continue;

            // Don't print passwords to the console
            final String shown = key.endsWith("password") ? "" : " (" + defaultValue + ")";
            plugin.getLogger().warning(key + " " + problem + ", using the default" + shown + ".");
            config.set(key, defaultValue);
            changed = true;
        }

        // The comments on the plugin's own settings are its documentation, so they follow the default config:
        // configs from older versions get new and updated explanations, and deleted ones come back.
        for (String key : defaults.getKeys(true)) {
            if (!config.getComments(key).equals(defaults.getComments(key))) {
                config.setComments(key, defaults.getComments(key));
                changed = true;
            }
            if (!config.getInlineComments(key).equals(defaults.getInlineComments(key))) {
                config.setInlineComments(key, defaults.getInlineComments(key));
                changed = true;
            }
        }

        if (!changed) return;
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
     * Loads the default config bundled with the plugin.
     *
     * @return The default config, or null if it couldn't be read
     */
    private static YamlConfiguration loadDefaults() {
        try (InputStream stream = plugin.getResource("config.yml")) {
            if (stream == null) throw new IllegalStateException("config.yml is missing from the plugin jar");
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to read the default config, skipping the config check: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks a value against its default.
     *
     * @param config       Configuration file
     * @param key          Key to check
     * @param defaultValue The key's value in the default config
     * @return What's wrong with the value, or null if it's fine
     */
    private static String findProblem(YamlConfiguration config, String key, Object defaultValue) {
        if (!config.contains(key)) return "is missing";

        final Object value = config.get(key);
        final boolean rightType = switch (defaultValue) {
            case Boolean ignored -> value instanceof Boolean;
            case Integer ignored -> value instanceof Integer;
            case List<?> ignored -> value instanceof List<?>;
            // Text can be anything that isn't a section or a list, e.g. a password that's only digits
            default -> value != null && !config.isConfigurationSection(key) && !(value instanceof List<?>);
        };
        if (!rightType) return "is not a valid " + typeName(defaultValue);

        final int[] range = RANGES.get(key);
        if (range != null) {
            final int number = (Integer) value;
            if (number < range[0] || number > range[1]) {
                return "is " + number + ", but should be between " + range[0] + " and " + range[1];
            }
        }
        return null;
    }

    private static String typeName(Object defaultValue) {
        return switch (defaultValue) {
            case Boolean ignored -> "true or false";
            case Integer ignored -> "whole number";
            case List<?> ignored -> "list";
            default -> "text";
        };
    }
}
