package com.chocohead.icbin1215;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.transformer.ClassInfo.Method;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import net.fabricmc.loader.api.FabricLoader;

import com.chocohead.icbin1215.MixinFinder.Mixin;

public class InterceptingMixinPlugin implements IMixinConfigPlugin {
	private @interface From {
		String method();
	}

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		ClassNode mixin;
		try {
			mixin = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName, false, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("Error fetching " + mixinClassName + " transforming " + targetClassName);
			e.printStackTrace();
			return true; //Possibly should be returning false if it can't be found?
		}

		AnnotationNode annotation = Annotations.getInvisible(mixin, InterceptingMixin.class);
		if (annotation == null) return true;

		String mod = Annotations.getValue(annotation, "from");
		return mod == null || FabricLoader.getInstance().isModLoaded(mod);
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		ClassNode thisMixin = Mixin.create(mixinInfo).getClassNode();

		AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
		if (interception == null) return; //Nothing to do for this particular Mixin

		Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception));
		on: for (MethodNode method : thisMixin.methods) {
			AnnotationNode surrogateNode = Annotations.getInvisible(method, PlacatingSurrogate.class);

			if (surrogateNode != null) {
				for (Method realMethod : interceptionMixin.getMethods()) {
					if (realMethod.getOriginalName().equals(method.name)) {
						Annotations.setInvisible(method, From.class, "method", method.name.concat(method.desc));
						method.name = realMethod.getName(); //Mangle name to whatever Mixin is using for the real injection
						method.invisibleAnnotations.remove(surrogateNode);
						Annotations.setVisible(method, Surrogate.class);

						targetClass.methods.add(method);
						continue on;
					}
				}

				throw new IllegalStateException("Cannot find original Mixin method for surrogate " + method.name + method.desc + " in " + interceptionMixin);	
			}
		}
	}

	private static Mixin findMixin(String targetClass, String mixinTarget) {
		for (Mixin mixin : MixinFinder.getMixinsFor(targetClass)) {
			if (mixinTarget.equals(mixin.getName())) {
				return mixin;
			}
		}

		throw new IllegalArgumentException("Can't find Mixin class " + mixinTarget + " targetting " + targetClass);
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		ClassNode thisMixin = Mixin.create(mixinInfo).getClassNode();

		AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
		if (interception == null) {
			for (MethodNode method : targetClass.methods) {
				if (Annotations.getInvisible(method, ButStatic.class) != null) {
					method.access |= Opcodes.ACC_STATIC;

					for (AbstractInsnNode insn : method.instructions) {
						if (insn.getType() == AbstractInsnNode.VAR_INSN) {
							((VarInsnNode) insn).var--;
						}
					}

					for (Iterator<LocalVariableNode> it = method.localVariables.iterator(); it.hasNext();) {
						if (it.next().index-- == 0) {
							it.remove();
						}
					}
				}
			}

			return; //Nothing more to do for this particular Mixin
		}

		Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception));
		Map<String, String> shims = thisMixin.methods.stream().filter(method -> Annotations.getInvisible(method, Shim.class) != null).collect(Collectors.toMap(method -> method.name.concat(method.desc), method -> {
			Method realMethod = interceptionMixin.getMethod(method.name, method.desc);

			if (realMethod == null) {
				throw new IllegalStateException("Cannot find shim method " + method.name + method.desc + " in " + interceptionMixin);
			}

			assert method.name.equals(realMethod.getOriginalName());
			return realMethod.getName();
		}));
		if (shims.isEmpty()) return; //Nothing to do

		Map<String, Consumer<MethodNode>> surrogates = new HashMap<>();
		targetClassName = targetClassName.replace('.', '/');

		for (Iterator<MethodNode> it = targetClass.methods.iterator(); it.hasNext();) {
			MethodNode method = it.next();

			AnnotationNode from;
			if (shims.containsKey(method.name.concat(method.desc))) {
				it.remove(); //Don't want to keep the shim methods
			} else if ((from = Annotations.getInvisible(method, From.class)) != null) {
				String origin = Annotations.getValue(from, "method");

				Consumer<MethodNode> copier = surrogates.remove(origin);
				if (copier != null) {
					copier.accept(method);
				} else {
					surrogates.put(origin, placatingSurrogate -> {
						method.instructions = placatingSurrogate.instructions;
						method.invisibleAnnotations.remove(from);
					});
				}
			} else {
				for (AbstractInsnNode insn : method.instructions) {
					if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
						MethodInsnNode methodInsn = (MethodInsnNode) insn;

						String replacedName = shims.get(methodInsn.name.concat(methodInsn.desc));
						if (replacedName != null && targetClassName.equals(methodInsn.owner)) {
							methodInsn.name = replacedName;
						}
					}
				}

				if (Annotations.getInvisible(method, PlacatingSurrogate.class) != null) {
					it.remove(); //Don't actually need the method itself, just its contents
					String origin = method.name.concat(method.desc);

					Consumer<MethodNode> copier = surrogates.remove(origin);
					if (copier != null) {
						copier.accept(method);
					} else {
						surrogates.put(origin, realSurrogate -> {
							realSurrogate.instructions = method.instructions;
							realSurrogate.invisibleAnnotations.remove(Annotations.getInvisible(realSurrogate, From.class));
						});
					}
				}
			}
		}
	}
}