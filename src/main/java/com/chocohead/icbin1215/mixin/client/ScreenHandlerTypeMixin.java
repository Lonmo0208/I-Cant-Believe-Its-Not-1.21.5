package com.chocohead.icbin1215.mixin.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType.Factory;

@Mixin(ScreenHandlerType.class)
abstract class ScreenHandlerTypeMixin<T extends ScreenHandler> {
	@Unique
	private Method actualFactory;

	@SuppressWarnings("unchecked")
	@WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerType$Factory;create(ILnet/minecraft/entity/player/PlayerInventory;Ljava/util/List;)Lnet/minecraft/screen/ScreenHandler;"))
	private T shimCreate(Factory<T> factory, int syncID, PlayerInventory inventory, List<Integer> mysteryList, Operation<T> original) throws Throwable {
		try {
			return original.call(factory, syncID, inventory, mysteryList);
		} catch (AbstractMethodError e) {			
		}

		try {
			if (actualFactory == null) {
				actualFactory = factory.getClass().getDeclaredMethod("create", int.class, PlayerInventory.class);
				actualFactory.setAccessible(true);
			}

			assert actualFactory.isAccessible(): factory + " (" + factory.getClass() + ')';
			return (T) actualFactory.invoke(factory, syncID, inventory);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} catch (ReflectiveOperationException | ClassCastException e) {
			throw new RuntimeException("Unable to shim ScreenHandler Factory " + factory + " (" + factory.getClass() + ')', e);
		}
	}
}