package moe.niso.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import moe.niso.NisoPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TablistManager {
    private static final NisoPlugin plugin = NisoPlugin.getInstance();

    private final BukkitTask task;

    public TablistManager() {
        final ConfigurationSection tablistConfig = plugin.getConfig().getConfigurationSection("tablist");

        if (tablistConfig == null || !tablistConfig.getBoolean("enabled")) {
            task = null;
            return;
        }

        int updateInterval = tablistConfig.getInt("update-interval");

        if (updateInterval < 20 || updateInterval > 1000) {
            updateInterval = 100;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateTablist(null);
            }
        }.runTaskTimerAsynchronously(plugin, 0, updateInterval);
    }

    public static void updateTablist(Player player) {
        final ConfigurationSection tablistConfig = plugin.getConfig().getConfigurationSection("tablist");

        if (tablistConfig == null) {
            return;
        }

        final boolean isEnabled = tablistConfig.getBoolean("enabled");
        final String header = tablistConfig.getString("header");
        final String footer = tablistConfig.getString("footer");

        if (!isEnabled || header == null || footer == null) {
            return;
        }

        if (player != null) {
            final Component headerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, header));
            final Component footerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, footer));
            player.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
        } else {
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                final Component headerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(onlinePlayer, header));
                final Component footerComponent = MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(onlinePlayer, footer));

                onlinePlayer.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
            }
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public static void setAllPlayerNames() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // Clear existing teams
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            setPlayerNames(player);
            addPlayerToTeam(scoreboard, player);
        }
    }

    private static int getPrimaryGroupWeight(Player player) {
        final CachedMetaData metaData = plugin.getLuckPerms().getPlayerAdapter(Player.class).getMetaData(player);
        final String primaryGroup = metaData.getPrimaryGroup();
        if (primaryGroup == null) {
            return Integer.MIN_VALUE;
        }

        final Group group = plugin.getLuckPerms().getGroupManager().getGroup(primaryGroup);
        if (group == null) {
            return Integer.MIN_VALUE;
        }

        return group.getWeight().orElse(Integer.MIN_VALUE);
    }

    public static void addPlayerToTeam(Scoreboard scoreboard, Player player) {
        int weight = getPrimaryGroupWeight(player);
        String teamName = getReversedTeamName(weight);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.addEntry(player.getName());
        setPlayerPrefix(team, player);
    }

    private static String getReversedTeamName(int weight) {
        // Reverse the weight by subtracting it from a large number
        int reversedWeight = Integer.MAX_VALUE - weight;
        return "weight_" + reversedWeight;
    }

    public static void setPlayerNames(Player player) {
        final CachedMetaData metaData = plugin.getLuckPerms().getPlayerAdapter(Player.class).getMetaData(player);
        final String primaryGroup = metaData.getPrimaryGroup();
        if (primaryGroup == null) {
            return;
        }

        final Group group = plugin.getLuckPerms().getGroupManager().getGroup(primaryGroup);
        if (group == null) {
            return;
        }

        final String prefix = metaData.getPrefix();
        if (prefix != null) {
            // Extract hex color from the prefix
            final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}");
            final Matcher matcher = hexPattern.matcher(prefix);

            if (!matcher.find()) {
                return;
            }

            final String hexColor = matcher.group();
            final TextColor color = TextColor.fromHexString(hexColor);

            if (color == null) {
                return;
            }

            final Component displayName = MiniMessage.miniMessage().deserialize(prefix).append(Component.space()).append(Component.text(player.getName()).color(color));
            player.playerListName(displayName);
        }
    }

    private static void setPlayerPrefix(Team team, Player player) {
        final CachedMetaData metaData = plugin.getLuckPerms().getPlayerAdapter(Player.class).getMetaData(player);
        final String prefix = metaData.getPrefix();
        if (prefix != null) {
            team.prefix(MiniMessage.miniMessage().deserialize(prefix).append(Component.space()));
        }
    }
}