package me.iceice666.rps

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import me.iceice666.rps.ModMain.LOGGER
import me.iceice666.rps.ModMain.ROOT_DIR
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket
import net.minecraft.server.MinecraftServer

import java.io.*
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.*
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipFile
import kotlin.io.path.isRegularFile

open class ResourcepackServer(private val config: Config) {

    var sha1: String? = null
    private var destination: String? = null
    private var resolvedDestination: Path? = null
    private var redirect: RedirectStrategy = RedirectStrategy.NO_OP

    companion object {
        private val RES_DIR = ROOT_DIR.resolve("rps/")
    }

    @Throws(IOException::class)
    fun load(destination: String): String {
        val dst = destination.trim()
        LOGGER.debug("Trying to load resourcepack...\nTarget: $dst")

        val resPath = when {
            dst.startsWith("http://") || dst.startsWith("https://") -> {

                LOGGER.info("This is a remote file!")
                val url = URI(dst).toURL()
                val tmpFile = RES_DIR.resolve("${dst}.zip").toFile()

                if (tmpFile.exists() && tmpFile.isFile) {
                    LOGGER.debug("Found cached resourcepack.")
                } else {
                    LOGGER.info("Downloading...")
                    url.openStream().use { inputStream ->
                        tmpFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                tmpFile.toPath()
            }

            else -> {
                LOGGER.info("This is a local file!")
                val targetFilePath = ROOT_DIR.resolve(dst)
                if (targetFilePath.isRegularFile(LinkOption.NOFOLLOW_LINKS) &&
                    targetFilePath.startsWith(ROOT_DIR)
                ) {
                    targetFilePath
                } else {
                    throw Exception("`$destination` seems not a file or not under the server root directory($ROOT_DIR).")
                }
            }
        }

        if (resPath == this.resolvedDestination) {
            return "Nothing changed"
        }

        zipCheck(resPath)
        val tmpSha1 = calculateSha1(resPath)

        // apply changes
        this.destination = dst
        this.resolvedDestination = resPath
        this.sha1 = tmpSha1
        this.redirect = RedirectStrategy.LOCAL

        return "ResourcePack changed to $resolvedDestination"
    }

    @Throws(Exception::class)
    fun zipCheck(targetPath: Path) {
        // Check if a valid resource pack
        ZipFile(targetPath.toFile()).use { zipFile ->
            if (!zipFile.entries().asSequence().any { it.name == "pack.mcmeta" }) {
                targetPath.toFile().delete()
                throw Exception("Invalid resource pack.")
            }
        }
    }

    @Throws(Exception::class)
    private fun calculateSha1(targetPath: Path): String {
        LOGGER.debug("Calculating sha1...")
        // calculate sha1
        val file = targetPath.toFile()
        val md = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        val sha1 = md.digest().joinToString("") { "%02x".format(it) }
        LOGGER.debug("Sha1: $sha1")

        return sha1
    }

    fun makePacket(packProperties: MinecraftServer.ServerResourcePackProperties): Optional<ResourcePackSendS2CPacket> {
        if (redirect == RedirectStrategy.NO_OP) {
            return Optional.empty()
        }

        return Optional.of(
            ResourcePackSendS2CPacket(
                //#if MC>=12100
                packProperties.id(),
                //#endif
                packProperties.url(),
                Objects.requireNonNull(this.sha1),
                packProperties.isRequired,
                Optional.ofNullable(packProperties.prompt())
            )
        )
    }

    fun getStatus() {

    }

    fun start() {
        server.start()
        LOGGER.info("ResourcePack server is running on port ${config.port}.")
        try {
            LOGGER.info(this.load(config.resourcepackPath))
        } catch (e: IOException) {
            LOGGER.error(e.toString())
        }
    }

    fun stop() {
        LOGGER.info("Stopping ResourcePack server")
        server.stop(0)
    }

    private val server: HttpServer = HttpServer.create(InetSocketAddress(config.port), 0).apply {
        createContext("/", CustomHandler(this@ResourcepackServer))
        executor = null
    }

    internal class CustomHandler(private val server: ResourcepackServer) : HttpHandler {

        @Throws(IOException::class)
        private fun handleLocal(exchange: HttpExchange) {
            val file = server.resolvedDestination?.toFile() ?: return make404Response(exchange)

            exchange.responseHeaders.apply {
                add("Content-Type", "application/zip")
                add("Content-Disposition", "attachment; filename=\"rps_${server.sha1}.zip\"")
            }
            exchange.sendResponseHeaders(200, file.length())

            file.inputStream().use { inputStream ->
                exchange.responseBody.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        private fun handleNoOp(exchange: HttpExchange) {
            exchange.sendResponseHeaders(200, -1)
        }


        private fun make404Response(exchange: HttpExchange) {
            val msg = "Requested resource not found"
            exchange.sendResponseHeaders(404, msg.length.toLong())
            exchange.responseBody.write(msg.toByteArray())
        }

        override fun handle(exchange: HttpExchange) {
            when (server.redirect) {
                RedirectStrategy.NO_OP -> handleNoOp(exchange)
                RedirectStrategy.LOCAL -> handleLocal(exchange)

            }

            exchange.close()
        }
    }

    enum class RedirectStrategy {
        NO_OP,
        LOCAL,

    }
}