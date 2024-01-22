package net.iceice666.resourcepackserver

import net.fabricmc.loader.api.FabricLoader
import net.iceice666.resourcepackserver.lib.Comment
import net.iceice666.resourcepackserver.lib.CustomConfig
import java.nio.file.Path

class ModConfig : CustomConfig {
    @JvmField
    @Comment("server port")
    var serverPort = 25566

    @JvmField
    @Comment("enable the server on startup")
    var enabled = true

    @JvmField
    @Comment(
        """
    resourcepack path
    It can be a local path or an url.
    """
    )
    var path = "server_resourcepack.zip"
    override val configFilePath: Path
        get() = FabricLoader.getInstance().configDir.resolve("resourcepack-server.properties")
}
