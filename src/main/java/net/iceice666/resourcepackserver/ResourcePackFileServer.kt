package net.iceice666.resourcepackserver

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import net.iceice666.resourcepackserver.lib.ConfigLoader
import org.slf4j.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.*

object ResourcePackFileServer {
    var configLoader = ConfigLoader(ModConfig())
    private val CONFIG = configLoader.loadConfig()
    private val LOGGER: Logger = Mod.LOGGER
    private var server: HttpServer? = null

    private var sha1 = ""
     var isLocalPath = true
    private var path = CONFIG.path

    @JvmStatic
    fun shouldRedirect(): Boolean {
        return !isLocalPath
    }

    @JvmStatic
    fun shouldOverwriteSha1(): Boolean {
        return CONFIG.overwriteSha1
    }

    @JvmStatic
    fun getPath(): String {
        return path
    }

    @JvmStatic
    fun getSha1(): String {
        return sha1
    }

    fun setSha1(s: String) {
        sha1 = s
    }

    fun setPath(location: String) {
        val s = location.trim { it <= ' ' }.lowercase(Locale.getDefault())
        isLocalPath = s.startsWith("http://") || s.startsWith("https://")
        path = location
    }

    fun calculateSha1() {
        try {
            val fis = FileInputStream(path)
            val sha1Digest = MessageDigest.getInstance("SHA-1")
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                sha1Digest.update(buffer, 0, bytesRead)
            }
            val sha1Hash = sha1Digest.digest()
            val hexString = StringBuilder()
            for (b in sha1Hash) {
                hexString.append(String.format("%02x", b))
            }
            sha1 = hexString.toString()
            LOGGER.info("Path of resourcepack is $path")
            LOGGER.info("SHA-1 of server resourcepack is $sha1")
            fis.close()
        } catch (e: FileNotFoundException) {
            LOGGER.error("Resourcepack not found")
            LOGGER.error("Please check your config file")
            LOGGER.error("Or run the command '/resourcepackserver set' to set correct resourcepack")
        } catch (e: Exception) {
            LOGGER.error(e.toString())
        }
    }

    fun start() {
        if (!isServerNeedToRun) {
            return
        }
        val port = CONFIG.serverPort


        // Create an HTTP server on the specified port
        try {
            server = HttpServer.create(InetSocketAddress(port), 0)

        } catch (e: IOException) {
            LOGGER.error("Failed to create file server", e)
            return
        }

        // Create a context for serving the file
        server!!.createContext("/", FileHandler())

        // Start the server
        server!!.setExecutor(null) // Use the default executor
        server!!.start()
        LOGGER.info("Resourcepack server is running on port $port")

        // Calculate sha1
        calculateSha1()

    }

    private val isServerNeedToRun: Boolean
        get() {
            // Check if the server is enabled
            if (!CONFIG.enabled) {
                LOGGER.info("Resourcepack server is disabled")
                return false
            }
            return true
        }

    fun stop() {
        if (server == null) return
        LOGGER.info("Stopping resourcepack server")
        server!!.stop(0)
    }
}

internal class FileHandler : HttpHandler {
    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {

        // Get the output stream for the response
        val os = exchange.responseBody

        // Get the headers of the HTTP request
        val headers = exchange.responseHeaders

        // Set the content type to "application/zip" (for ZIP files)
        headers["Content-Type"] = "application/zip"

        // Set the Content-Disposition header to prompt for download
        headers["Content-Disposition"] = "attachment; filename=server_resourcepack.zip"


        // Get the file to be served (replace with the actual file path)
        val file = File(ResourcePackFileServer.getPath())


        // Send the file as the response
        exchange.sendResponseHeaders(200, file.length())
        file.toURI().toURL().openStream().use { `is` ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (`is`.read(buffer).also { bytesRead = it } != -1) {
                os.write(buffer, 0, bytesRead)
            }
        }
        os.close()
    }
}
