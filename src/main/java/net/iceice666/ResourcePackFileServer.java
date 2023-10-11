package net.iceice666;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class ResourcePackFileServer {

    public static final Logger LOGGER = LoggerFactory.getLogger("resourcepack-server");
    static HttpServer server;

    public static void start() {

        if (!isServerNeedToRun()) {
            return;
        }
        // Create the server

        int port = 25566; // Port on which the server will listen


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

        // Generate SHA-1 of server resourcepack
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

            String sha1 = hexString.toString();

            LOGGER.info("SHA-1 of server resourcepack: " + sha1);

            // Write SHA-1 to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("server_resourcepack.sha1.txt"))) {
                writer.write(sha1);
            }

            fis.close();
        } catch (Exception ignored) {
        }
    }

    static boolean isServerNeedToRun() {
        // Check if server_resourcepack.zip exists
        return Files.exists(Paths.get("server_resourcepack.zip"));
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
