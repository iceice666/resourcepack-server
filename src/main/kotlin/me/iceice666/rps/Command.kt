package me.iceice666.rps

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import me.iceice666.rps.ModMain.RPS
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object Command {

    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal<ServerCommandSource>("rps")
                .requires { source: ServerCommandSource -> source.hasPermissionLevel(3) }
                .executes { context: CommandContext<ServerCommandSource> -> execHelp(context) }

                .then(
                    literal<ServerCommandSource>("status")
                        .executes { context: CommandContext<ServerCommandSource> -> execStatus(context) }
                )

                .then(
                    literal<ServerCommandSource>("load")
                        .then(
                            argument<ServerCommandSource, String>("uri", StringArgumentType.string())
                                .executes { context: CommandContext<ServerCommandSource> -> execLoad(context) }
                        )
                )

                .then(
                    literal<ServerCommandSource>("help")
                        .executes { context: CommandContext<ServerCommandSource> -> execHelp(context) }
                )


        )

    }

    private fun execLoad(context: CommandContext<ServerCommandSource>): Int {
        val uri = StringArgumentType.getString(context, "uri")
        val executor = context.source

        val msg = Text.of(
            try {
                RPS.load(uri)
            } catch (e: Exception) {
                e.toString()
            }
        )

        executor.sendFeedback({ msg }, true)

        return SINGLE_SUCCESS
    }

    private fun execStatus(context: CommandContext<ServerCommandSource>): Int {
        TODO("Not yet implemented")

        return SINGLE_SUCCESS
    }

    private fun execHelp(context: CommandContext<ServerCommandSource>): Int {
        context.source.sendFeedback({
            Text.of(
                """
                Available commands:
                  help => Show this message.
                  start => Start the server.
                  stop => Stop the server.
                  status => Check server info.
                  reload => Check if the resource pack is valid and recalculate the sha1.
                  load <path> => Load server resource pack from a local path or remote url.
                                When set to a local path, beware that the path will not expand to absolute path.
                                '.' and '..' still works.                           
                                (e.g. `~/my-resource-pack` will not work, use `/home/<username>/my-resource-pack` instead.)
                                When set to a url, the server will download the resource pack and treat as a local file.
                """.trimIndent()
            )
        }, false)

        return SINGLE_SUCCESS
    }

}