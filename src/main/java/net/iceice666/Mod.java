package net.iceice666;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements DedicatedServerModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("resourcepack-server");


    @Override
    public void onInitializeServer() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                net.iceice666.Command.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ResourcePackFileServer.start());

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ResourcePackFileServer.stop();
            ResourcePackFileServer.configLoader.saveConfig();
        });
    }


}


