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
        TODO("Not yet implemented")

        return SINGLE_SUCCESS
    }

}