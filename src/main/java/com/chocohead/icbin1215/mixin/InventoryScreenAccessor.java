package com.chocohead.icbin1215.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

@Mixin(InventoryScreen.class)
public interface InventoryScreenAccessor {
	@Accessor
	ButtonWidget getField_59329();
}