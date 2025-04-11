package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.DynamicRegistryManager.Immutable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.GameInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
	@Shadow
	protected GameInstance gameInstance;

	public ServerWorld method_3847(RegistryKey<World> key) {
		return gameInstance.getWorld(key);
	}

	public ServerRecipeManager method_3772() {
		return gameInstance.getRecipeManager();
	}

	public PlayerManager method_3760() {
		return gameInstance.getPlayerManager();
	}

	public Immutable method_30611() {
		return gameInstance.getRegistryManager();
	}

	public SaveProperties method_27728() {
		return gameInstance.getSaveProperties();
	}
}