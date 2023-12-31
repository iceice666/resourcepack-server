package net.iceice666.resourcepackserver.mixin;

import net.iceice666.resourcepackserver.ResourcePackFileServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import java.util.Optional;

@Mixin(ServerPropertiesHandler.class)
public abstract class OverwriteResourcePackProperties {

    private OverwriteResourcePackProperties(){}

    @Shadow
    @Final
    static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "getServerResourcePackProperties", cancellable = true)
    private static void getServerResourcePackProperties(
            String url,
            String sha1,
            @Nullable String hash,
            boolean required,
            String prompt,
            CallbackInfoReturnable<Optional<MinecraftServer.ServerResourcePackProperties>> cir) {
        if (!ResourcePackFileServer.isServerRunning()) cir.setReturnValue(Optional.empty());

        if (ResourcePackFileServer.shouldOverwriteSha1()) {

            Text text = ServerPropertiesHandler.parseResourcePackPrompt(prompt);


            cir.setReturnValue(Optional.of(new MinecraftServer.ServerResourcePackProperties(
                    ResourcePackFileServer.shouldRedirect() ? ResourcePackFileServer.getPath() : url,
                    ResourcePackFileServer.getSha1(),
                    required,
                    text)));
        }

    }
}