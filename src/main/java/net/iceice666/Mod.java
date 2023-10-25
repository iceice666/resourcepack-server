package net.iceice666;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class Mod implements DedicatedServerModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("resourcepack-server");


    @Override
    public void onInitializeServer() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                MyCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ResourcePackFileServer.start());

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ResourcePackFileServer.configLoader.saveConfig();
            ResourcePackFileServer.stop();
        });
    }


    static class MyCommand {

        // This function defines your command
        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
            dispatcher.register(
                    literal("calcSha1")
                            .requires(source -> source.hasPermissionLevel(1))
                            .executes(context -> execute()
                            ));
        }

        // This function processes the command
        private static int execute() {
            LOGGER.info("Re-calculate SHA-1 of server resourcepack...");
            ResourcePackFileServer.calculateSha1();
            return Command.SINGLE_SUCCESS;
        }

    }
}


