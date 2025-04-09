package com.chocohead.drummondmill.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

@Mixin(World.class)
abstract class WorldMixin {
	public MinecraftServer method_8503() {
		return null;
	}
}