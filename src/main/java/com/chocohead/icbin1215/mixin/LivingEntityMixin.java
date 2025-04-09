package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {
	private LivingEntityMixin() {
		super(null, null);
	}

	@Shadow
	public abstract ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership, boolean skipClientDrop);

	public ItemEntity method_7329(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
		return dropItem(stack, dropAtSelf, retainOwnership, true);
	}
}