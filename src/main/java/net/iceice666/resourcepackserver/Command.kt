package net.iceice666.resourcepackserver

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.slf4j.Logger

object Command {
    private val LOGGER: Logger = Mod.LOGGER
    private const val SINGLE_SUCCESS = Command.SINGLE_SUCCESS

    // This function defines your command
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("resourcepackserver")
                .requires { source: ServerCommandSource -> source.hasPermissionLevel(1) }
                .then(
                    CommandManager.literal("refreshSha1")
                        .executes { executeRecalc() }
                ).then(
                    CommandManager.literal("set").then(
                        CommandManager.literal("local").then(
                            CommandManager.argument("local_path", StringArgumentType.string())
                                .executes { context -> executeSetLocal(context) }
                        )
                    )
                        .then(
                            CommandManager.literal("url").then(
                                CommandManager.argument("url_path", StringArgumentType.string())
                                    .then(
                                        CommandManager.argument("sha1", StringArgumentType.string())
                                            .executes { context -> executeSetUrl(context) }
                                    )
                            )
                        )
                )
        )
    }


    private fun executeSetLocal(context: CommandContext<ServerCommandSource>): Int {
        val localPath = StringArgumentType.getString(context, "local_path")
        LOGGER.info("Set local resourcepack path to $localPath")
        ResourcePackFileServer.setPath(localPath)
        ResourcePackFileServer.calculateSha1()
        return SINGLE_SUCCESS
    }

    private fun executeSetUrl(context: CommandContext<ServerCommandSource>): Int {
        val urlPath = StringArgumentType.getString(context, "url_path")
        val sha1 = StringArgumentType.getString(context, "sha1")
        LOGGER.info("Set url resourcepack path to $urlPath")
        ResourcePackFileServer.sha1 = sha1
        ResourcePackFileServer.setPath(urlPath)
        return SINGLE_SUCCESS
    }


    private fun executeRecalc(): Int {
        LOGGER.info("Re-calculate SHA-1 of server resourcepack...")
        ResourcePackFileServer.calculateSha1()
        return SINGLE_SUCCESS
    }
}
