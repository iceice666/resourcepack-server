package net.iceice666;

import draylar.omegaconfig.api.Comment;
import draylar.omegaconfig.api.Config;

public class ModConfig implements Config {

    @Comment("The port to run the resourcepack server on.")
    int serverPort = 25566;

    @Override
    public String getName() {
        return "resourcepack-server";
    }
}
