package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.class_10972;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerCommonNetworkHandler;

@Mixin(ServerCommonNetworkHandler.class)
abstract class ServerCommonNetworkHandlerMixin extends class_10972 {
	private ServerCommonNetworkHandlerMixin() {
		super(null, null);
	}

	public void method_52391(Packet<?> packet, PacketCallbacks callbacks) {
		method_69158(packet, callbacks);
	}
}