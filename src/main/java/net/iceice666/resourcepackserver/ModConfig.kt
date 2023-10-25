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
    @Comment("enable the server")
    var enabled = true

    @JvmField
    @Comment(
        """
    overwrite the sha1 of server resourcepack
    if this is true, the server will calculate the sha1 of server_resourcepack.zip
    and send it to client. and if you have been set a sha1 in server.properties, it will be ignored.
                                         
    This option will not be affected by the 'enabled' option.
    """
    )
    var overwriteSha1 = true

    @JvmField
    @Comment(
        """
    resourcepack path
    It's can be a local path or a url path.
    """
    )
    var path = "server_resourcepack.zip"
    override val configFilePath: Path
        get() = FabricLoader.getInstance().configDir.resolve("resourcepack-server.properties")
}
