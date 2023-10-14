package net.iceice666.mixin;

import net.iceice666.ResourcePackFileServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin({MinecraftServer.class, ServerPropertiesHandler.class})
public abstract class ModMixin {
    @Mixin(MinecraftServer.class)
    static class overwriteServer {
        @Inject(at = @At("HEAD"), method = "stop")
        private void stopResourcePackFileServer(CallbackInfo info) {
            ResourcePackFileServer.stop();
        }
    }

    @Mixin(ServerPropertiesHandler.class)
    static class overwriteProperties {
        @Inject(at = @At("HEAD"), method = "getServerResourcePackProperties", cancellable = true)
        private static void getServerResourcePackProperties(String url, String sha1, @Nullable String hash, boolean required, String prompt, CallbackInfoReturnable<Optional<MinecraftServer.ServerResourcePackProperties>> cir) {
            if (ResourcePackFileServer.CONFIG.overwriteSha1) {

                Text text = ServerPropertiesHandler.parseResourcePackPrompt(prompt);
                cir.setReturnValue(Optional.of(new MinecraftServer.ServerResourcePackProperties(
                        url,
                        ResourcePackFileServer.getSha1(),
                        required,
                        text)));
            }
        }
    }
}