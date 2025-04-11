package com.chocohead.icbin1215.compat;

import java.util.Collections;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;

import com.chocohead.icbin1215.mixin.InventoryScreenAccessor;

public class REIPlugin implements REIClientPlugin {
	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(InventoryScreen.class, screen -> {
			ButtonWidget playerUnlock = ((InventoryScreenAccessor) screen).getField_59329();
			return Collections.singletonList(new Rectangle(playerUnlock.getX(), playerUnlock.getY(), playerUnlock.getWidth(), playerUnlock.getHeight()));
		});
		zones.register(CreativeInventoryScreen.class, screen -> {
			ButtonWidget playerUnlock = ((InventoryScreenAccessor) screen).getField_59329();

			if (playerUnlock.visible) {
				return Collections.singletonList(new Rectangle(playerUnlock.getX(), playerUnlock.getY(), playerUnlock.getWidth(), playerUnlock.getHeight()));
			} else {
				return Collections.emptyList();
			}
		});
	}
}