package com.chocohead.drummondmill.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity {
	private ServerPlayerEntityMixin() {
		super(null, null, 0, null);
	}

	public MinecraftServer method_5682() {
		return method_69130().getServer();
	}
}