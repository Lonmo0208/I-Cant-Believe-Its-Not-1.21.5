package com.chocohead.icbin1215.mixin.client;

import java.util.Arrays;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.gui.screen.world.WorldCreator.Mode;

@Mixin(targets = "net/minecraft/client/gui/screen/world/CreateWorldScreen$GameTab")
abstract class GameTabMixin {
	@ModifyArg(method = "<init>", allow = 1,
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/WorldCreator$Mode;HARDCORE:Lnet/minecraft/client/gui/screen/world/WorldCreator$Mode;", opcode = Opcodes.GETSTATIC)),
			at = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/client/gui/widget/CyclingButtonWidget$Builder;values([Ljava/lang/Object;)Lnet/minecraft/client/gui/widget/CyclingButtonWidget$Builder;"))
	private static Object[] addCreativeMode(Object... values) {
		values = Arrays.copyOf(values, values.length + 1);
		values[values.length - 1] = Mode.CREATIVE;
		return values;
	}
}