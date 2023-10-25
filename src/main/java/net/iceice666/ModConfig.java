
package net.iceice666;

import net.fabricmc.loader.api.FabricLoader;
import net.iceice666.lib.Comment;
import net.iceice666.lib.CustomConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class ModConfig implements CustomConfig {


    @Comment("server port")
    public int serverPort = 25566;

    @Comment("enable the server")
    public boolean enabled = true;

    @Comment(
            """
                            overwrite the sha1 of server resourcepack
                            if this is true, the server will calculate the sha1 of server_resourcepack.zip
                            and send it to client. and if you have been set a sha1 in server.properties, it will be ignored.
                                         
                            This option will not be affected by the 'enabled' option.
                    """
    )
    public boolean overwriteSha1 = true;

    @Comment("""
resourcepack path
It's can be a local path or a url path.
""")
    public String path = "server_resourcepack.zip";


    @NotNull
    @Override
    public Path getConfigFilePath() {
        return FabricLoader.getInstance().getConfigDir().resolve("resourcepack-server.properties");
    }
}
