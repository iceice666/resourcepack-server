package net.iceice666;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class ResourcePackFileServer {

    public static final ModConfig CONFIG = new ModConfig();
    static final Logger LOGGER = LoggerFactory.getLogger("resourcepack-server");
    static HttpServer server = null;

    static String sha1 = "";

    public static String getSha1() {
        return sha1;
    }

    public static void start() {

        if (!isServerNeedToRun()) {
            return;
        }

        int port = CONFIG.serverPort;


        // Create an HTTP server on the specified port
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            LOGGER.error("Failed to create file server", e);
            return;
        }

        // Create a context for serving the file
        server.createContext("/", new FileHandler());

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        LOGGER.info("Resourcepack server is running on port " + port);

        // Calculate SHA-1 of server resourcepack
        try {
            FileInputStream fis = new FileInputStream("server_resourcepack.zip");
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                sha1Digest.update(buffer, 0, bytesRead);
            }

            byte[] sha1Hash = sha1Digest.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : sha1Hash) {
                hexString.append(String.format("%02x", b));
            }

            sha1 = hexString.toString();

            LOGGER.info("SHA-1 of server resourcepack: " + sha1);


            fis.close();
        } catch (Exception ignored) {
        }

    }

    static boolean isServerNeedToRun() {
        // Check if server is enabled
        if (!CONFIG.enabled) {
            LOGGER.info("Resourcepack server is disabled");
            return false;
        }
        // Check if server_resourcepack.zip exists
        else if (!Files.exists(Paths.get("server_resourcepack.zip"))) {
            LOGGER.error("server_resourcepack.zip not found");
            LOGGER.error("Please put server_resourcepack.zip in the server root directory");
            LOGGER.info("Resourcepack server is disabled");
            return false;
        } else {
            return true;
        }
    }

    public static void stop() {
        if (server == null) return;
        LOGGER.info("Stopping resourcepack server");
        server.stop(0);
    }
}

class FileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Get the output stream for the response
        OutputStream os = exchange.getResponseBody();

        // Get the headers of the HTTP request
        Headers headers = exchange.getResponseHeaders();

        // Set the content type to "application/zip" (for ZIP files)
        headers.set("Content-Type", "application/zip");

        // Set the Content-Disposition header to prompt for download
        headers.set("Content-Disposition", "attachment; filename=server_resourcepack.zip");

        // Get the file to be served (replace with the actual file path)
        File file = new File("server_resourcepack.zip");

        // Send the file as the response
        exchange.sendResponseHeaders(200, file.length());
        try (var is = file.toURI().toURL().openStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        os.close();
    }
}