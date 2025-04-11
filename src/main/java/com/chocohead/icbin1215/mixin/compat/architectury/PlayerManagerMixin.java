package com.chocohead.icbin1215.mixin.compat.architectury;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;

import com.chocohead.icbin1215.InterceptingMixin;
import com.chocohead.icbin1215.PlacatingSurrogate;
import com.chocohead.icbin1215.Shim;

@Mixin(PlayerManager.class)
@InterceptingMixin(value = "dev/architectury/mixin/fabric/MixinPlayerList", from = "architectury")
abstract class PlayerManagerMixin {
	@PlacatingSurrogate
	private void respawn(ServerPlayerEntity player, boolean alive, RemovalReason removalReason, Optional<TeleportTarget> target, CallbackInfoReturnable<ServerPlayerEntity> call) {
		respawn(player, alive, removalReason, call);
	}

	@Shim
    private native void respawn(ServerPlayerEntity player, boolean alive, RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> call);
}