package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.GameInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin {
	@Shadow
	private @Final GameInstance gameInstance;

	public MinecraftServer method_8503() {
		return gameInstance.getServer();
	}
}