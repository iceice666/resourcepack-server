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
                                
                # calculate resourcepack sha1
                # If you want, this mod can calculate the sha1 of the server resourcepack for you.
                # but it will apply to your `server.properties` file, so you have to manually copy it to your `server.properties` file.
                calculateSha1=true
                """;
    }


    public int serverPort = CONFIG.getOrDefault("serverPort", 25566);
    public boolean enabled = CONFIG.getOrDefault("enabled", true);
    public boolean calculateSha1 = CONFIG.getOrDefault("calculateSha1", true);
}
