package net.iceice666.resourcepackserver

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import net.iceice666.resourcepackserver.Mod.Companion.GAME_DIR
import net.iceice666.resourcepackserver.lib.ConfigLoader
import org.slf4j.Logger
import java.io.*
import java.net.InetSocketAddress
import java.net.URL
import java.net.URLConnection
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipFile
import kotlin.io.path.isReadable


// Object declaration for a singleton pattern that manages a resource pack file server.
object ResourcePackFileServer {
    private var configLoader = ConfigLoader(ModConfig())
    private val CONFIG = configLoader.loadConfig()
    private val LOGGER: Logger = Mod.LOGGER
    private var server: HttpServer? = null

    private var sha1 = ""

    // origin
    private var originPath = ""

    // resolved
    private var resPath = ""

    private val gameAbsDir = GAME_DIR.toAbsolutePath()
        .toString().removeSuffix(".")

    // Getter for the server path.
    @JvmStatic
    fun getOriginPath(): String = originPath

    @JvmStatic
    fun getResPath(): String = resPath

    // Getter for the SHA-1 checksum.
    @JvmStatic
    fun getSha1(): String = sha1

    @JvmStatic
    fun isServerRunning(): Boolean = server != null

    // Setter for the path of the resource pack, with string normalization.
    fun setPath(location: String): String {
        val tmpPath = location.trim()
        // Nothing changed, return
        if (originPath == tmpPath) return "Nothing changed."

        // A remote source
        if (tmpPath.startsWith("http://") || tmpPath.startsWith("https://")) {
            try {
                downloadFile(resPath, "tmpfile-res.zip")
            } catch (e: IOException) {
                return "Failed to download file: \n" + e.message
            }
            resPath = "tmpfile-res.zip"
        } else {
            val tmpPath = Paths.get(tmpPath).toAbsolutePath().normalize()
            if (!tmpPath.startsWith(gameAbsDir)) {
                return "Cannot resolve the file path($tmpPath).\n" +
                        "Please ensure the file is under the game directory($gameAbsDir)\n" +
                        "and ends with '.zip'."
            }



            resPath = tmpPath.toString()
        }

        return calculateSha1()

    }

    private fun containsPackMcmeta(path: String = resPath): Boolean {
        val zipFile = ZipFile(path)
        val entries = zipFile.entries()

        var res = false

        while (entries.hasMoreElements()) {
            if ("pack.mcmeta" == entries.nextElement().name) {
                res = true
                break
            }
        }

        zipFile.close()

        return res
    }

    // Calculates the SHA-1 checksum of the file at the given path.
    fun calculateSha1(path: String = resPath): String {
        val resPath = Paths.get(path)
        if (!resPath.isReadable()) {
            return "Cannot read the file($resPath).\n" +
                    "Please ensure the file exists and is readable."
        }

        if (!containsPackMcmeta()) {
            return "File is not a valid resource pack."
        }

        val fis = FileInputStream(path)
        val sha1Digest = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (fis.read(buffer).also { bytesRead = it } != -1) {
            sha1Digest.update(buffer, 0, bytesRead)
        }

        val sha1Hash = sha1Digest.digest()
        sha1 = sha1Hash.joinToString("") { "%02x".format(it) }

        return "Path of resourcepack is $path\n" +
                "SHA-1 of server resourcepack is $sha1"

    }

    // Starts the HTTP server if needed.
    fun start(force: Boolean = false) {
        if (!force && !CONFIG.enabled) {
            LOGGER.info("Resourcepack server is disabled")
            return

        }
        val port = CONFIG.serverPort

        try {
            server = HttpServer.create(InetSocketAddress(port), 0).apply {
                createContext("/", FileHandler())
                executor = null // Use the default executor
                start()
            }
        } catch (e: IOException) {
            LOGGER.error("Failed to start file server", e)
            return
        }

        LOGGER.info("Resourcepack server is running on port $port")
        val msg = setPath(CONFIG.path)
        LOGGER.info(msg)


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


@Throws(IOException::class)
fun downloadFile(fileUrl: String, destinationPath: String) {
    val url = URL(fileUrl)
    val connection: URLConnection = url.openConnection()
    connection.getInputStream().use { `in` ->
        FileOutputStream(destinationPath).use { out ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while ((`in`.read(buffer).also { bytesRead = it }) != -1) {
                out.write(buffer, 0, bytesRead)
            }
        }
    }
}

// Handler class that responds to HTTP requests with the resource pack file.
internal class FileHandler : HttpHandler {
    @Throws(IOException::class)
    override fun handle(exchange: HttpExchange) {
        val file = File(ResourcePackFileServer.getResPath())

        // Send the response headers before writing data to the response output stream.
        exchange.responseHeaders.apply {
            put("Content-Type", listOf("application/zip"))
            put("Content-Disposition", listOf("attachment; filename=\"${ResourcePackFileServer.getSha1()}.zip\""))
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
