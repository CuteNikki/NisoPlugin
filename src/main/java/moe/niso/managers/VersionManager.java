package moe.niso.managers;

import moe.niso.NisoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class VersionManager {
    private static final NisoPlugin plugin = NisoPlugin.getInstance();
    private static final String repositoryOwner = "CuteNikki";

    /**
     * Gets the download URL for the plugin.
     *
     * @return The download URL
     */
    public static String getDownloadURL() {
        return "https://github.com/" + repositoryOwner + "/" + plugin.getPluginMeta().getName() + "/releases/latest/download/" + plugin.getPluginMeta().getName() + ".jar";
    }

    /**
     * Gets the version URL for the plugin.
     *
     * @return The version URL
     */
    public static String getVersionURL() {
        return "https://github.com/" + repositoryOwner + "/" + plugin.getPluginMeta().getName() + "/releases/latest";
    }

    /**
     * Checks if the current version is newer than the old version.
     *
     * @param oldVersion The old version
     * @param newVersion The new version
     * @return True if the new version is newer, false otherwise
     */
    public static boolean isNewerVersion(String oldVersion, String newVersion) {
        String[] oldParts = oldVersion.substring(1).split("\\.");
        String[] newParts = newVersion.substring(1).split("\\.");

        for (int i = 0; i < Math.max(oldParts.length, newParts.length); i++) {
            int oldPart = i < oldParts.length ? Integer.parseInt(oldParts[i]) : 0;
            int newPart = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;
            if (newPart > oldPart) {
                return true;
            } else if (newPart < oldPart) {
                return false;
            }
        }
        return false;
    }

    /**
     * Gets the current version of the plugin.
     *
     * @return The current version
     */
    public static String getCurrentVersion() {
        return "v" + plugin.getPluginMeta().getVersion();
    }

    /**
     * Gets the newest version of the plugin from GitHub.
     *
     * @return The newest version, or null if GitHub couldn't be reached
     */
    public static String getNewestVersion() {
        try {
            // The latest release's page redirects to its tag, e.g. .../releases/tag/v1.5.4. Unlike GitHub's API, it
            // isn't limited to 60 requests an hour per IP address.
            final HttpURLConnection connection = (HttpURLConnection) new URI(getVersionURL()).toURL().openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(10_000);
            try {
                final String location = connection.getHeaderField("Location");
                final String version = location == null ? null : location.substring(location.lastIndexOf('/') + 1);
                if (version == null || !location.contains("/releases/tag/") || !version.matches("v\\d+(\\.\\d+)*")) {
                    plugin.getLogger().warning(plugin.logPrefixUpdater + "Failed to fetch latest version. Response code: " + connection.getResponseCode());
                    return null;
                }
                return version;
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            // Not being able to check for updates shouldn't stop the plugin from working
            plugin.getLogger().warning(plugin.logPrefixUpdater + "Error fetching latest version: " + e.getMessage());
            return null;
        }
    }

    /**
     * Downloads a version from GitHub into the server's update folder (plugins/update), under the same file name as
     * the running jar. Paper swaps it in on the next start, so one restart applies it, and the running jar is never
     * overwritten. The download is only kept if it's a jar of the expected version.
     *
     * @param version The version to download, e.g. v1.5.5
     * @return True if the update was downloaded successfully, false otherwise
     */
    public static boolean downloadUpdate(String version) {
        final File updateFolder = Bukkit.getUpdateFolderFile();
        final File target = new File(updateFolder, plugin.getJarFile().getName());
        final File partial = new File(updateFolder, target.getName() + ".part");
        final String url = "https://github.com/" + repositoryOwner + "/" + plugin.getPluginMeta().getName() + "/releases/download/" + version + "/" + plugin.getPluginMeta().getName() + ".jar";

        try {
            Files.createDirectories(updateFolder.toPath());

            final HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            try {
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    plugin.getLogger().warning(plugin.logPrefixUpdater + "Download failed. Response code: " + connection.getResponseCode());
                    return false;
                }
                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(inputStream, partial.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                connection.disconnect();
            }

            // A failed or cut-off download must never end up where Paper would load it
            final String downloadedVersion = readPluginVersion(partial);
            if (!version.equals("v" + downloadedVersion)) {
                plugin.getLogger().warning(plugin.logPrefixUpdater + "The download isn't " + version + " (found " + downloadedVersion + "), so it was discarded.");
                Files.deleteIfExists(partial.toPath());
                return false;
            }

            Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            plugin.getLogger().info(plugin.logPrefixUpdater + "Update downloaded successfully!");
            return true;
        } catch (IOException | URISyntaxException e) {
            plugin.getLogger().severe(plugin.logPrefixUpdater + "Error downloading file: " + e.getMessage());
            partial.delete();
            return false;
        }
    }

    /**
     * Reads the version from a plugin jar's plugin.yml.
     *
     * @param jar The jar to read
     * @return The version, or null if the file isn't a plugin jar
     */
    private static String readPluginVersion(File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            final JarEntry entry = jarFile.getJarEntry("plugin.yml");
            if (entry == null) return null;
            try (Reader reader = new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(reader).getString("version");
            }
        } catch (IOException e) {
            return null;
        }
    }
}
