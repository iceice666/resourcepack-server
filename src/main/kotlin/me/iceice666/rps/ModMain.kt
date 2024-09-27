package me.iceice666.rps

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

object ModMain : DedicatedServerModInitializer {

    val LOGGER: Logger = LoggerFactory.getLogger("rps")

    val ROOT_DIR: Path = FabricLoader.getInstance().gameDir.toAbsolutePath()

    private val config: Config = Config.new()


    @JvmField
    val RPS = ResourcepackServer(config)

    override fun onInitializeServer() {
        LOGGER.info("Initializing resourcepack server")

        ServerLifecycleEvents.SERVER_STARTED.register(
            ServerLifecycleEvents.ServerStarted { RPS.start() }
        )

        ServerLifecycleEvents.SERVER_STOPPING.register(
            ServerLifecycleEvents.ServerStopping { RPS.stop() }
        )

        CommandRegistrationCallback.EVENT.register { dispatcher: CommandDispatcher<ServerCommandSource>,
                                                     _: CommandRegistryAccess,
                                                     _: CommandManager.RegistrationEnvironment
            ->
            Command.register(dispatcher)
        }

    }

}