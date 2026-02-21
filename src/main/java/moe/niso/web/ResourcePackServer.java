package moe.niso.web;

import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import moe.niso.NisoPlugin;

public class ResourcePackServer {

    private final NisoPlugin plugin = NisoPlugin.getInstance();
    private HttpServer server;
    private byte[] cachedHash;

    /**
     * Starts the HTTP server on the specified port.
     *
     * @param port The port number to start the server on.
     */
    public void start(int port) {
        try {
            String fileName = plugin.getConfig()
                .getString("resource-pack.file-name", "resource_pack.zip");
            File resourcePackFile = new File(plugin.getDataFolder(), fileName);

            if (resourcePackFile.exists()) {
                cachedHash = calculateFileHash(resourcePackFile);
            } else {
                cachedHash = new byte[0];
                plugin.getLogger().warning("Resource pack file not found during startup!");
            }

            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/resource_pack.zip", exchange -> {
                if (!resourcePackFile.exists()) {
                    String response = "Resource pack not found.";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    plugin.getLogger()
                        .severe("Resource pack not found at " + resourcePackFile.getAbsolutePath());
                    return;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders()
                    .set("Content-Disposition", "attachment; filename=\"resource_pack.zip\"");
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");

                exchange.sendResponseHeaders(200, resourcePackFile.length());
                try (OutputStream outputStream = exchange.getResponseBody(); FileInputStream fileInputStream = new FileInputStream(
                    resourcePackFile)) {
                    fileInputStream.transferTo(outputStream);
                }
                exchange.close();
            });
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("Resource Pack Server started on port " + port);
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
