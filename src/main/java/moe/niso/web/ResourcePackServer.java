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

public class ResourcePackServer {
    private final NisoPlugin plugin = NisoPlugin.getInstance();
    private HttpServer server;

    /**
     * Starts the HTTP server on the specified port.
     * @param port The port number to start the server on.
     */
    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/resource_pack.zip", exchange -> {
                File resourcePackFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("resource-pack.file-name", "resource_pack.zip"));
                if (!resourcePackFile.exists()) {
                    String response = "Resource pack not found.";
                    exchange.sendResponseHeaders(404, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    plugin.getLogger().severe("Resource pack not found at " + resourcePackFile.getAbsolutePath());
                }

                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"resource_pack.zip\"");
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");

                exchange.sendResponseHeaders(200, resourcePackFile.length());
                try (OutputStream outputStream = exchange.getResponseBody(); FileInputStream fileInputStream = new FileInputStream(resourcePackFile)) {
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
     * Computes the SHA-1 hash of a file.
     * By providing an SHA-1 hash in the setResourcePack method, Minecraft will check the hash of your local file against the one from the server.
     * If they don't match, it triggers an automatic redownload.
     *
     * @param file The file to hash.
     * @return The SHA-1 hash as a hexadecimal string.
     */
    public static byte[] getFileHash(File file) {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return digest.digest(); // Returns the 20-byte array
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
