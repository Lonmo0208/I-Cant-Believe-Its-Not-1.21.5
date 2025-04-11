package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.class_1959$class_5482;
import net.minecraft.world.biome.Biome;

@Mixin(Biome.class)
abstract class BiomeMixin {
	@SuppressWarnings("unused")
	private class_1959$class_5482 field_26393;
}