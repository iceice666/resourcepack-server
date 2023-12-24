package net.iceice666.resourcepackserver

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager.RegistrationEnvironment
import net.minecraft.server.command.ServerCommandSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Mod : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher: CommandDispatcher<ServerCommandSource?>, _: CommandRegistryAccess, _: RegistrationEnvironment ->
            Command.register(dispatcher)
        })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerStarted { ResourcePackFileServer.start() })
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerStopping {
            ResourcePackFileServer.stop()
        })
    }

    companion object {

        @JvmField
        val LOGGER: Logger = LoggerFactory.getLogger("resourcepack-server")
    }
}
