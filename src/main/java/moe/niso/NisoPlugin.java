package moe.niso;

import moe.niso.commands.HomeCommand;
import moe.niso.commands.WarpCommand;
import moe.niso.listeners.ChatListener;
import moe.niso.listeners.JoinListener;
import moe.niso.listeners.LeaveListener;
import moe.niso.listeners.MotdListener;
import moe.niso.managers.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class NisoPlugin extends JavaPlugin {
    private static NisoPlugin instance;

    private String messagePrefix;
    private String chatFormat;
    private String welcomeMessage;
    private String leaveMessage;
    private String serverMotd;

    private DatabaseManager databaseManager;

    /**
     * Get the plugin instance.
     *
     * @return Plugin instance
     */
    public static NisoPlugin getInstance() {
        return instance;
    }

    /**
     * Called when the plugin is loading.
     */
    @Override
    public void onLoad() {
        getLogger().info("Plugin is loading...");
    }

    /**
     * Called when the plugin is enabled / the server is starting up.
     */
    @Override
    public void onEnable() {
        // Set the instance variable to this instance
        instance = this;

        // Create default configuration file if it doesn't exist
        saveDefaultConfig();

        // Check if the PostgreSQL driver is loaded and disable the plugin if it's not found
        try {
            Class.forName("org.postgresql.Driver");
            getLogger().info("PostgreSQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            getLogger().severe("PostgreSQL Driver not found: " + e.getMessage());
            getLogger().warning("Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Check if PlaceholderAPI is installed and disable the plugin if it's not
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().warning("PlaceholderAPI is not installed! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Cache configuration values to prevent unnecessary file access on each join, leave, etc.
        messagePrefix = getConfig().getString("message-prefix");
        chatFormat = getConfig().getString("chat-format");
        welcomeMessage = getConfig().getString("welcome-message");
        leaveMessage = getConfig().getString("leave-message");
        serverMotd = getConfig().getString("server-motd");

        // Initialize and set up database manager
        databaseManager = new DatabaseManager();
        databaseManager.setupDatabase();
        // Create tables if they don't exist
        databaseManager.createHomesTable();
        databaseManager.createWarpsTable();

        // Register events and commands
        registerEvents();
        registerCommands();

        getLogger().info("Plugin is enabled!");
    }

    /**
     * Called when the plugin is disabled / the server is shutting down.
     */
    @Override
    public void onDisable() {
        // Close the database connection pool
        if (databaseManager != null) {
            databaseManager.closeDatabase();
        }

        getLogger().info("Plugin is disabled!");
    }

    /**
     * Register all commands.
     */
    private void registerCommands() {
        getCmd("home").setExecutor(new HomeCommand());
        getCmd("warp").setExecutor(new WarpCommand());

        getLogger().info("Commands registered!");
    }

    /**
     * Register all event listeners.
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new MotdListener(), this);

        getLogger().info("Events registered!");
    }

    /**
     * Get a command by name.
     *
     * @param name Command name
     * @return PluginCommand instance
     */
    private PluginCommand getCmd(String name) {
        return Objects.requireNonNull(getCommand(name), "Command '" + name + "' is not registered in plugin.yml");
    }

    /**
     * Get the database manager instance.
     *
     * @return DatabaseManager instance
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Get the message prefix.
     *
     * @return Message prefix
     */
    public String getChatFormat() {
        return chatFormat;
    }

    /**
     * Get the welcome message.
     *
     * @return Welcome message
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * Get the leave message.
     *
     * @return Leave message
     */
    public String getLeaveMessage() {
        return leaveMessage;
    }

    /**
     * Get the server MOTD.
     *
     * @return Server MOTD
     */
    public String getServerMotd() {
        return serverMotd;
    }

    /**
     * Prefix a message with the message prefix.
     *
     * @param component Component to prefix
     * @return Prefixed component
     */
    public Component prefixMessage(Component component) {
        return MiniMessage.miniMessage().deserialize(messagePrefix).append(component);
    }
}