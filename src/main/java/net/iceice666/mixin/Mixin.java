package net.iceice666.mixin;

import net.iceice666.ResourcePackFileServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@org.spongepowered.asm.mixin.Mixin(MinecraftServer.class)
public class Mixin {
    @Inject(at = @At("HEAD"), method = "stop")
    private void init(CallbackInfo info) {
        ResourcePackFileServer.stop();
    }
}