package net.iceice666;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Command {
    public static final Logger LOGGER = LoggerFactory.getLogger("resourcepack-server");
    private static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

    // This function defines your command
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("resourcepackserver")
                        .requires(source -> source.hasPermissionLevel(1))
                        .then(
                                literal("refreshSha1")
                                        .executes(context -> execute_recalc())
                        ).then(
                                literal("set").then(
                                                literal("local").then(
                                                        argument("local_path", string())
                                                                .executes(Command::execute_set_local)
                                                )
                                        )
                                        .then(
                                                literal("url").then(
                                                        argument("url_path", string())
                                                                .then(
                                                                        argument("sha1", string())
                                                                                .executes(Command::execute_set_url)
                                                                )
                                                )
                                        )
                        )
        );
    }

    private static int execute_set_local(CommandContext<ServerCommandSource> context) {
        String local_path = getString(context, "local_path");

        LOGGER.info("Set local resourcepack path to " + local_path);

        ResourcePackFileServer.setPath(local_path);
        ResourcePackFileServer.calculateSha1();

        return SINGLE_SUCCESS;
    }

    private static int execute_set_url(CommandContext<ServerCommandSource> context) {
        String url_path = getString(context, "url_path");
        String sha1 = getString(context, "sha1");
        LOGGER.info("Set url resourcepack path to " + url_path);

        ResourcePackFileServer.setSha1(sha1);
        ResourcePackFileServer.setPath(url_path);


        return SINGLE_SUCCESS;
    }

    // This function processes the command
    private static int execute_recalc() {
        LOGGER.info("Re-calculate SHA-1 of server resourcepack...");
        ResourcePackFileServer.calculateSha1();
        return SINGLE_SUCCESS;
    }

}
