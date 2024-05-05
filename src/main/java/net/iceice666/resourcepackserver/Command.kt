package net.iceice666.resourcepackserver

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.iceice666.resourcepackserver.ResourcePackFileServer.getOriginPath
import net.iceice666.resourcepackserver.ResourcePackFileServer.getResPath
import net.iceice666.resourcepackserver.ResourcePackFileServer.getSha1
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
            literal("rps")
                .requires { source: ServerCommandSource -> source.hasPermissionLevel(1) }
                .executes { executeHelp(it) }
                .then(
                    literal("reload")
                        .executes { executeReload(it) }
                ).then(
                    literal("load")
                        .requires { source: ServerCommandSource -> source.hasPermissionLevel(3) }
                        .then(
                            argument("uri", StringArgumentType.string())
                                .executes { context -> executeLoad(context) }
                        )

                ).then(
                    literal("info").executes { context -> executeInfo(context) }
                )
                .then(

                    literal("start")
                        .requires { source: ServerCommandSource -> source.hasPermissionLevel(3) }
                        .executes {
                            if (!ResourcePackFileServer.isServerRunning()) {
                                it.source.sendFeedback( Text.of("Starting server!") , true)
                                ResourcePackFileServer.start(true)
                            } else {
                                it.source.sendFeedback( Text.of("The server is currently running.") , true)
                            }


                            return@executes 1

              

                        }
                ).then(

                    literal("stop")
                        .requires { source: ServerCommandSource -> source.hasPermissionLevel(3) }
                        .executes {
                            if (ResourcePackFileServer.isServerRunning()) {
                                it.source.sendFeedback( Text.of("Stopping server!") , true)
                                ResourcePackFileServer.stop()
                            } else {
                                it.source.sendFeedback( Text.of("The server hasn't started yet.") , true)
                            }
                            return@executes 1

                        }
                ).then(
                    literal("help").executes { executeHelp(it) }
                )
        )
    }


    private fun executeHelp(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback(
            Text.of(
                """
                Available commands:
                  help => Show this message.
                  start => Start the server.
                  stop => Stop the server.
                  info => Check server info.
                  reload => Check if the resource pack is valid and recalculate the sha1.
                  load <path> => Load server resource pack from a local path or remote url.
                                When set to a local path, beware that the path will not expand to absolute path.
                                '.' and '..' still works.                           
                                (e.g. `~/my-resource-pack` will not work, use `/home/<username>/my-resource-pack` instead.)
                                When set to a url, the server will download the resource pack and treat as a local file.
                """.trimIndent()
            )
        , true)
        return 1
    }

    private fun executeInfo(context: CommandContext<ServerCommandSource>): Int {
        if (!ResourcePackFileServer.isServerRunning()) {
            context.source.sendFeedback( Text.literal("ResourcePackServer is not running!") , true)
        } else {
            val text = Text.of("\n") as MutableText
            val resPath = getResPath()
            text.append("Current path:  ${getOriginPath()} ${if (resPath != "") "(${resPath})" else ""}")

            text.append("\n")
            val sha1 = getSha1()
            text.append(
                "Sha1: ${if (sha1 != "") sha1 else "Not set yet."}"
            )

            context.source.sendFeedback( text , true)


        }
        return SINGLE_SUCCESS
    }

    private fun commandSetReminder(source: ServerCommandSource): Boolean {
        val text =
            (Text.of("Warning! The path set by command will not saved to the config file!") as MutableText)
                .setStyle(
                    Style.EMPTY
                        .withColor(Formatting.YELLOW)
                )

        source.sendFeedback( text , true)

        return true

    }

    private fun executeLoad(context: CommandContext<ServerCommandSource>): Int {
        commandSetReminder(context.source)
        val uri = StringArgumentType.getString(context, "uri")
        context.source.sendFeedback( Text.of(ResourcePackFileServer.setPath(uri)) , true)

        return SINGLE_SUCCESS
    }

    private fun executeReload(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback( Text.of(ResourcePackFileServer.calculateSha1()) , true)
        return SINGLE_SUCCESS
    }
}
