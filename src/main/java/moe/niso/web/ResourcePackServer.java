package moe.niso.web;

import com.sun.net.httpserver.HttpServer;
import moe.niso.NisoPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.Executors;

public class ResourcePackServer {

    private final NisoPlugin plugin = NisoPlugin.getInstance();
    private HttpServer server;
    private byte[] cachedHash;

    /**
     * Helper to get a clean, sanitized filename without leading slashes or whitespace.
     */
    public String getSanitizedFileName() {
        String raw = plugin.getConfig().getString("resource-pack.file-name", "resource_pack.zip");
        if (raw == null || raw.isBlank()) {
            return "resource_pack.zip";
        }
        return raw.trim().replaceAll("^/+", "");
    }

    /**
     * Starts the HTTP server on the specified port.
     *
     * @param port The port number to start the server on.
     */
    public void start(int port) {
        try {
            String fileName = getSanitizedFileName();
            File resourcePackFile = new File(plugin.getDataFolder(), fileName);

            reloadHash();

            server = HttpServer.create(new InetSocketAddress(port), 0);

            String contextPath = "/" + fileName;
            server.createContext(contextPath, exchange -> {
                if (!resourcePackFile.exists()) {
                    String response = "Resource pack not found.";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    plugin.getLogger().severe("Resource pack request failed: file not found at " + resourcePackFile.getAbsolutePath());
                    return;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");

                exchange.sendResponseHeaders(200, resourcePackFile.length());
                try (OutputStream outputStream = exchange.getResponseBody();
                     FileInputStream fileInputStream = new FileInputStream(resourcePackFile)) {
                    fileInputStream.transferTo(outputStream);
                }
                exchange.close();
            });

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            plugin.getLogger().info("Resource Pack Server started on port " + port + " listening on " + contextPath);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start Resource Pack Server: " + e.getMessage());
        }
    }

    /**
     * Stops the HTTP server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            plugin.getLogger().info("Resource Pack Server stopped.");
        }
    }

    /**
     * Re-calculates and caches the SHA-1 hash from disk. Useful during plugin reloads.
     */
    public void reloadHash() {
        String fileName = getSanitizedFileName();
        File resourcePackFile = new File(plugin.getDataFolder(), fileName);

        if (resourcePackFile.exists()) {
            cachedHash = calculateFileHash(resourcePackFile);
            String hexHash = HexFormat.of().formatHex(cachedHash);
            plugin.getLogger().info("Calculated Resource Pack SHA-1: " + hexHash);
        } else {
            cachedHash = new byte[0];
            plugin.getLogger().warning("Resource pack file not found at " + resourcePackFile.getAbsolutePath());
        }
    }

    /**
     * Retrieves the cached SHA-1 hash of the resource pack.
     */
    public byte[] getCachedHash() {
        return cachedHash != null ? cachedHash : new byte[0];
    }

    /**
     * Computes the SHA-1 hash of a file.
     */
    private byte[] calculateFileHash(File file) {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return digest.digest();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to calculate resource pack hash: " + e.getMessage());
            return new byte[0];
        }
    }
}