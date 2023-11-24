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
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
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
                    literal("set")
                        .then(
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
                    literal("info").executes { context -> executeInfo(context) }
                )
                .then(
                    literal("start").executes {
                        ResourcePackFileServer.start(true)
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

    private fun executeInfo(context: CommandContext<ServerCommandSource>): Int {
        if (!ResourcePackFileServer.isServerRunning()) {
            if (context.source.isExecutedByPlayer)
                context.source.player?.sendMessage(Text.literal("ResourcePackServer is not running!"))
            else
                LOGGER.warn("ResourcePackServer is not running!")
        } else {

            if (context.source.isExecutedByPlayer) {

                val player = context.source.player

                val text = Text.of("") as MutableText
                text.append("Current path:" + getPath())

                if (shouldRedirect())
                    text.append(
                        (Text.of("   Redirected") as MutableText)
                            .setStyle(
                                Style.EMPTY
                                    .withColor(Formatting.GREEN)
                            )
                    )


                text.append("\n")
                text.append("Sha1:" + getSha1())


                player?.sendMessage(text)
            } else
                LOGGER.info(
                    """
                Current path: ${getPath()} ${if (shouldRedirect()) "   (Redirected)" else ""}
                Sha1: ${getSha1()}
                
            """.trimIndent()
                )

        }
        return SINGLE_SUCCESS
    }

    private fun commandSetReminder(source: ServerCommandSource): Boolean {
        val text =
            (Text.of("Warning! The path set by command will not be written back to the config file!") as MutableText)
                .setStyle(
                    Style.EMPTY
                        .withColor(Formatting.YELLOW)
                )

        if (source.isExecutedByPlayer) source.player?.sendMessage(text)
        else LOGGER.warn("Warning! The path set by command will not be written back to the config file!")

        return true

    }

    private fun executeSetLocal(context: CommandContext<ServerCommandSource>): Int {
        commandSetReminder(context.source)
        val localPath = StringArgumentType.getString(context, "local_path")
        LOGGER.info("Set local resourcepack path to $localPath")
        ResourcePackFileServer.setPath(localPath)
        ResourcePackFileServer.calculateSha1()
        return SINGLE_SUCCESS
    }

    private fun executeSetUrl(context: CommandContext<ServerCommandSource>): Int {
        commandSetReminder(context.source)
        val urlPath = StringArgumentType.getString(context, "url_path")
        val sha1 = StringArgumentType.getString(context, "sha1")
        LOGGER.info("Set url resourcepack path to $urlPath")
        LOGGER.info("Set url resourcepack sha1 to $sha1")
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
