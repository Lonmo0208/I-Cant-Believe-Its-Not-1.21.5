package com.chocohead.icbin1215;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

public class InterceptingMixinPlugin implements IMixinConfigPlugin {
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
		return true;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
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
	}
}