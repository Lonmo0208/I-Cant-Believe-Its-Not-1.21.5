package com.chocohead.icbin1215;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.util.Annotations;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAtNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;

public class FixBrokenTargets implements MixinAnnotationAdjuster {
	private static String applyRefmap(List<String> targetClasses, String mixinClass, String reference) {
		assert targetClasses.size() >= 1: mixinClass;
		return applyRefmap(targetClasses.get(0), mixinClass, reference);
	}

	@SuppressWarnings("unchecked")
	private static String applyRefmap(String targetClass, String mixinClass, String reference) {
		ClassInfo target = ClassInfo.fromCache(targetClass);
		assert target != null: targetClass + " mixin'd by " + mixinClass;

		Set<IMixinInfo> mixins;
		try {
			mixins = (Set<IMixinInfo>) FieldUtils.readDeclaredField(target, "mixins", true);
		} catch (ReflectiveOperationException | ClassCastException e) {
			throw new RuntimeException("Error trying to read mixins targetting " + target, e);
		}

		for (IMixinInfo mixin : mixins) {
			if (mixinClass.equals(mixin.getClassName())) {
				try {
					return (String) MethodUtils.invokeMethod(mixin, true, "remapClassName", reference);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Error applying refmap from " + mixin + " to " + reference, e.getCause());
				} catch (ReflectiveOperationException | ClassCastException e) {
					throw new RuntimeException("Error trying to apply refmap from " + mixin + " to " + reference, e);
				}
			}
		}

		throw new AssertionError("Unable to find " + mixinClass + " in " + mixins + " (targetting " + target + ')');
	}

	private static AdjustableAnnotationNode unwrappingSugar(AdjustableAnnotationNode annotation, UnaryOperator<AdjustableAnnotationNode> action) {
		if ("Lcom/llamalad7/mixinextras/sugar/impl/SugarWrapper;".equals(annotation.desc)) {
			AnnotationNode realAnnotation = Annotations.getValue(annotation, "original");
			assert realAnnotation != null: annotation;
			annotation.set("original", action.apply(AdjustableAnnotationNode.fromNode(realAnnotation)));
			return annotation;
		} else {
			return action.apply(annotation);
		}
	}

	@Override
	public AdjustableAnnotationNode adjust(List<String> targetClasses, String mixinClass, MethodNode handler, AdjustableAnnotationNode annotation) {
		switch (mixinClass) {
		case "fuzs.puzzleslib.fabric.mixin.client.ClientLevelFabricMixin": {
			if (!annotation.is(Inject.class)) break; //Some other kind of injector
			AdjustableInjectNode inject = annotation.as(AdjustableInjectNode.class);
			if (!inject.getMethod().contains("<init>")) break; //Some other injection

			Type[] args = Type.getArgumentTypes(handler.desc);
			Type[] newArgs = new Type[args.length + 2];
			System.arraycopy(args, 0, newArgs, 0, args.length - 1);
			newArgs[newArgs.length - 1] = args[args.length - 1];
			newArgs[args.length] = newArgs[args.length - 1] = Type.getType(List.class);
			//handler.desc = Type.getMethodDescriptor(Type.VOID_TYPE, newArgs);
			break;
		}
		case "fuzs.puzzleslib.fabric.mixin.ServerPlayerFabricMixin": {
			annotation = unwrappingSugar(annotation, node -> {
				if (!node.is(Inject.class)) return node; //Some other kind of injector
				AdjustableInjectNode inject = node.as(AdjustableInjectNode.class);
				if (!inject.getMethod().contains("drop(Z)Z")) return node; //Some other injection

				return inject.withAt(locations -> {
					assert locations.size() == 1: locations;
					AdjustableAtNode at = locations.get(0);

					String target = at.getTarget();
					assert "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;".equals(target): target;
					at.setTarget(applyRefmap(targetClasses, mixinClass, target).replace("ZZ)", "ZZZ)"));

					return locations;
				});
			});
			break;
		}
		}

		return annotation;
	}
}