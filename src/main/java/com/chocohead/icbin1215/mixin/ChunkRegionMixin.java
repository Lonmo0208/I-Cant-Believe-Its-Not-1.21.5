package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkRegion;

@Mixin(ChunkRegion.class)
abstract class ChunkRegionMixin {
	@Shadow
	private @Final ServerWorld world;

	public MinecraftServer method_8503() {
		return world.getGameInstance().getServer();
	}
}