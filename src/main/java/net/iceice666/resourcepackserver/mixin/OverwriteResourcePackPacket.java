package net.iceice666.resourcepackserver.mixin;

import net.iceice666.resourcepackserver.ResourcePackFileServer;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class OverwriteResourcePackPacket {

    private OverwriteResourcePackPacket() {
    }


    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Inject(at = @At("HEAD"), method = "sendResourcePackUrl(Ljava/lang/String;Ljava/lang/String;ZLnet/minecraft/text/Text;)V", cancellable = true)
    private void send(String url, String hash, boolean required, Text resourcePackPrompt, CallbackInfo ci) {
        if (ResourcePackFileServer.isServerRunning()) {

            networkHandler.sendPacket(new ResourcePackSendS2CPacket(
                    url,
                    ResourcePackFileServer.getSha1(),
                    required,
                    resourcePackPrompt));

            ci.cancel();
        }
    }

}