package net.iceice666.resourcepackserver

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.iceice666.resourcepackserver.ResourcePackFileServer.getPath
import net.iceice666.resourcepackserver.ResourcePackFileServer.getSha1
import net.iceice666.resourcepackserver.ResourcePackFileServer.shouldRedirect
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.slf4j.Logger

object Command {
    private val LOGGER: Logger = Mod.LOGGER
    private const val SINGLE_SUCCESS = Command.SINGLE_SUCCESS

    // This function defines your command
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            literal("resourcepackserver")
                .requires { source: ServerCommandSource -> source.hasPermissionLevel(1) }
                .then(
                    literal("refreshSha1")
                        .executes { executeRecalc() }
                ).then(
                    literal("set").then(
                        literal("local").then(
                            argument("local_path", StringArgumentType.string())
                                .executes { context -> executeSetLocal(context) }
                        )
                    )
                        .then(
                            literal("url").then(
                                argument("url_path", StringArgumentType.string())
                                    .then(
                                        argument("sha1", StringArgumentType.string())
                                            .executes { context -> executeSetUrl(context) }
                                    )
                            )
                        )
                ).then(
                    literal("debug").executes { context -> executeDebug(context) }
                )
                .then(
                    literal("start").executes {
                        ResourcePackFileServer.start()
                        return@executes 1
                    }
                ).then(
                    literal("stop").executes {
                        ResourcePackFileServer.stop()
                        return@executes 1
                    }
                )
        )
    }

    private fun executeDebug(context: CommandContext<ServerCommandSource>): Int {
        val player = context.source.player
        player?.sendMessage(Text.of("should redirect: " + shouldRedirect()))
        player?.sendMessage(Text.of("target path: " + getPath()))
        player?.sendMessage(Text.of("sha1: " + getSha1()))

        return SINGLE_SUCCESS
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
        ResourcePackFileServer.setSha1(sha1)
        ResourcePackFileServer.setPath(urlPath)
        return SINGLE_SUCCESS
    }


    private fun executeRecalc(): Int {
        LOGGER.info("Re-calculate SHA-1 of server resourcepack...")
        ResourcePackFileServer.calculateSha1()
        return SINGLE_SUCCESS
    }
}
