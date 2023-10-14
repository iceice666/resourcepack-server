package net.iceice666;

import net.iceice666.lib.SimpleConfig;

public class ModConfig {

    SimpleConfig CONFIG = SimpleConfig.of("resourcepack-server").provider(this::provider).request();

    private String provider(String filename) {
        return """
                #default config
                                
                # enable the server
                enabled=true
                                
                # server port
                serverPort=25566
                
                # overwrite the sha1 of server resourcepack
                # if this is true, the server will calculate the sha1 of server_resourcepack.zip
                # and send it to client. and if you have been set a sha1 in server.properties, it will be ignored.
                #
                # This option will not be affected by the 'enabled' option.
                overwriteSha1=true
                """;
    }


    public int serverPort = CONFIG.getOrDefault("serverPort", 25566);
    public boolean enabled = CONFIG.getOrDefault("enabled", true);
    public boolean overwriteSha1 = CONFIG.getOrDefault("overwriteSha1", true);
}
