package com.chocohead.icbin1215.mixin.client;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.unlock.UnlocksScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.item.ItemGroup.Type;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;

@Mixin(CreativeInventoryScreen.class)
abstract class CreativeInventoryScreenMixin extends HandledScreen<CreativeScreenHandler> implements FabricCreativeInventoryScreen {
	@Unique
	private final ButtonWidget unlocksButton = TextIconButtonWidget.builder(UnlocksScreen.field_59413, button -> {
		client.setScreen(new UnlocksScreen(client.player.networkHandler.getUnlockHandler(), this));
	}, true).texture(Identifier.ofVanilla("icon/player_unlocks"), 24, 24)
			.dimension(24, 24)
			.method_70263(Tooltip.of(UnlocksScreen.field_59413))
			.build();

	private CreativeInventoryScreenMixin() {
		super(null, null, null);
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;init()V", shift = Shift.AFTER))
	private void onInit(CallbackInfo call) {
		addSelectableChild(unlocksButton);
	}

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;statusEffectsDisplay:Lnet/minecraft/client/gui/screen/ingame/StatusEffectsDisplay;", opcode = Opcodes.GETFIELD))
	private void renderUnlocksButton(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo call) {
		if (getSelectedItemGroup().getType() == Type.INVENTORY) {
			unlocksButton.visible = true;
			unlocksButton.setPosition(x + backgroundWidth + 3, y - 22 - 1);
			unlocksButton.render(context, mouseX, mouseY, deltaTicks);
		} else {
			unlocksButton.visible = false;
		}
	}
}