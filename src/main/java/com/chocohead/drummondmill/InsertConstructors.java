package com.chocohead.drummondmill;

import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.MethodNode;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import com.chocohead.mm.api.ClassTinkerers;

public class InsertConstructors implements Runnable {
	private static Type typeForClassName(MappingResolver remapper, String name) {
		return Type.getObjectType(remapper.mapClassName("intermediary", name).replace('.', '/'));
	}

	private static MethodNode appendFloat(String owner, Type... args) {
		Type[] argsWithFloat = Arrays.copyOf(args, args.length + 1);
		argsWithFloat[args.length] = Type.FLOAT_TYPE;
		MethodNode out = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, args), null, null);
		InstructionAdapter method = new InstructionAdapter(out);

		method.load(0, InstructionAdapter.OBJECT_TYPE);
		int slot = 1;
		for (Type arg : args) {
			method.load(slot, arg);
			slot += arg.getSize();
		}
		method.fconst(0);
		method.invokespecial(owner, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, argsWithFloat), false);
		method.areturn(Type.VOID_TYPE);

		return out;
	}

	private static void addFloatlessConstructor(String owner, Type... args) {
		ClassTinkerers.addTransformation(owner, node -> {
			assert node.methods.stream().anyMatch(method -> {
				if (!"<init>".equals(method.name)) return false;
				Type[] methodArgs = Type.getArgumentTypes(method.desc);
				if (methodArgs.length == 0 || methodArgs[methodArgs.length - 1] != Type.FLOAT_TYPE) return false;
				return Arrays.equals(Arrays.copyOf(methodArgs, methodArgs.length - 1), args);
			}): node.name;
			node.methods.add(appendFloat(node.name, args));
		});
	}

	@Override
	public void run() {
		MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

		Type registryEntry = typeForClassName(remapper, "net.minecraft.class_6880");
		Type tagKey = typeForClassName(remapper, "net.minecraft.class_6862");
		Type registryKey = typeForClassName(remapper, "net.minecraft.class_5321");

		String armourMaterial = remapper.mapClassName("intermediary", "net.minecraft.class_1741");
		addFloatlessConstructor(armourMaterial, Type.INT_TYPE, Type.getType(Map.class), Type.INT_TYPE, registryEntry, Type.FLOAT_TYPE, Type.FLOAT_TYPE, tagKey, registryKey);
		String toolMaterial = remapper.mapClassName("intermediary", "net.minecraft.class_9886");
		addFloatlessConstructor(toolMaterial, tagKey, Type.INT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.INT_TYPE, tagKey);
	}
}