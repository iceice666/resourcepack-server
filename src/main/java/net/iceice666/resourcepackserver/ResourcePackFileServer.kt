package net.iceice666.resourcepackserver

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import net.iceice666.resourcepackserver.lib.ConfigLoader
import org.slf4j.Logger
import java.io.*
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.*

// Object declaration for a singleton pattern that manages a resource pack file server.
object ResourcePackFileServer {
    var configLoader = ConfigLoader(ModConfig())
    private val CONFIG = configLoader.loadConfig()
    private val LOGGER: Logger = Mod.LOGGER
    private var server: HttpServer? = null

    private var sha1 = ""
    private var isLocalPath = true
    private var path = CONFIG.path

    // Redirects only if the path is not local.
    @JvmStatic
    fun shouldRedirect(): Boolean = !isLocalPath

    // Determines if the SHA-1 should be overwritten based on the configuration.
    @JvmStatic
    fun shouldOverwriteSha1(): Boolean = CONFIG.overwriteSha1

    // Getter for the server path.
    @JvmStatic
    fun getPath(): String = path

    // Getter for the SHA-1 checksum.
    @JvmStatic
    fun getSha1(): String = sha1

    // Setter for the SHA-1 checksum.
    fun setSha1(s: String) {
        sha1 = s
    }


    fun isServerRunning(): Boolean = server != null

    // Setter for the path of the resource pack, with string normalization.
    fun setPath(location: String) {
        path = location.trim().lowercase(Locale.getDefault())
        isLocalPath = !path.startsWith("http://") && !path.startsWith("https://")
    }

    // Calculates the SHA-1 checksum of the file at the given path.
    fun calculateSha1() {
        try {
            FileInputStream(path).use { fis ->
                val sha1Digest = MessageDigest.getInstance("SHA-1")
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    sha1Digest.update(buffer, 0, bytesRead)
                }
                val sha1Hash = sha1Digest.digest()
                sha1 = sha1Hash.joinToString("") { "%02x".format(it) }
                LOGGER.info("Path of resourcepack is $path")
                LOGGER.info("SHA-1 of server resourcepack is $sha1")
            }
        } catch (e: FileNotFoundException) {
            LOGGER.error(
                """
                Resourcepack not found. 
                Please check your config file or run the command '/resourcepackserver set' to set the correct resourcepack.
                """.trimIndent()
            )
        } catch (e: Exception) {
            LOGGER.error("An error occurred while calculating SHA-1: ${e.message}")
        }
    }

    // Starts the HTTP server if needed.
    fun start(force: Boolean = false) {
        if (!force && !CONFIG.enabled) {

            LOGGER.info("Resourcepack server is disabled")
            return

        }

        try {
            val port = CONFIG.serverPort
            server = HttpServer.create(InetSocketAddress(port), 0).apply {
                createContext("/", FileHandler())
                executor = null // Use the default executor
                start()
            }
            LOGGER.info("Resourcepack server is running on port $port")
            calculateSha1()
        } catch (e: IOException) {
            LOGGER.error("Failed to start file server", e)
        }
    }

    // Stops the server if it's running.
    fun stop() {
        server?.let {
            LOGGER.info("Stopping resourcepack server")
            it.stop(0)

            // Set server to null
            server = null
        }


    }
}

// Handler class that responds to HTTP requests with the resource pack file.
internal class FileHandler : HttpHandler {
    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {
        val path = ResourcePackFileServer.getPath()
        // Check if the path is a URL
        if (path.startsWith("http://") || path.startsWith("https://")) {
            // Redirect to the URL
            exchange.responseHeaders.add("Location", path)
            exchange.sendResponseHeaders(302, -1) // -1 indicates no response body
            exchange.close()

            return
        }

        val file = File(ResourcePackFileServer.getPath())

        // Send the response headers before writing data to the response output stream.
        exchange.responseHeaders.apply {
            put("Content-Type", listOf("application/zip"))
            put("Content-Disposition", listOf("attachment; filename=\"server_resourcepack.zip\""))
        }
        exchange.sendResponseHeaders(200, file.length())

        // Use a BufferedInputStream for efficient file reading.
        BufferedInputStream(FileInputStream(file)).use { bis ->
            BufferedOutputStream(exchange.responseBody).use { bos ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (bis.read(buffer).also { bytesRead = it } != -1) {
                    bos.write(buffer, 0, bytesRead)
                }
            }
        }
    }
}
