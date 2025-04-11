package com.chocohead.icbin1215.mixin;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;

import com.chocohead.icbin1215.ButStatic;

@Mixin(RegistryEntryList.class)
interface RegistryEntryListMixin {
	@Shadow
	static <T> RegistryEntryList.Direct<T> of(Collection<? extends RegistryEntry<T>> collection) {
		throw new AssertionError();
	}

	@ButStatic
	default <T> RegistryEntryList.Direct<T> method_40242(List<? extends RegistryEntry<T>> collection) {
		return of(collection);
	}
}