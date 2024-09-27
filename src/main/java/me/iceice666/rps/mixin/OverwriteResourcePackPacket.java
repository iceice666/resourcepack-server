package me.iceice666.rps.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.SendResourcePackTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

import static me.iceice666.rps.ModMain.RPS;


@Mixin(SendResourcePackTask.class)
public abstract class OverwriteResourcePackPacket {

	private OverwriteResourcePackPacket() {
	}


	@Shadow
	@Final
	private MinecraftServer.ServerResourcePackProperties packProperties;

	@Inject(at = @At("HEAD"), method = "sendPacket(Ljava/util/function/Consumer;)V", cancellable = true)
	private void hash(Consumer<Packet<?>> sender, CallbackInfo ci) {
		Optional<ResourcePackSendS2CPacket> packet = RPS.makePacket(packProperties);

		if (packet.isPresent()) {
			sender.accept(packet.get());
			ci.cancel();
		}


	}
}