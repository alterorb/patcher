package net.alterorb.patcher.transformer;

import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

public class CacheRedirector implements Transformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheRedirector.class);

    private static final String JAGEX_STORE = ".jagex_cache_";

    @Override
    public void transform(FunOrbGame game, List<ClassNode> classNodes) {
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        classNode.methods.stream()
                         .filter(this::hasJagexStoreLdc)
                         .findFirst()
                         .ifPresent(this::transform);
    }

    private void transform(MethodNode methodNode) {
        var fileParamIdx = findFileParamIdx(methodNode);

        if (fileParamIdx == -1) {
            throw new IllegalStateException("Could not locate the file param idx");
        }
        var argumentTypes = Type.getArgumentTypes(methodNode.desc);
        var stringType = Type.getType(String.class);
        var directoryParamIdx = -1;

        for (int idx = 0; idx < argumentTypes.length; idx++) {
            if (idx != fileParamIdx && argumentTypes[idx].equals(stringType)) {
                directoryParamIdx = idx;
                break;
            }
        }
        if (directoryParamIdx == -1) {
            throw new IllegalStateException("Could not distinguish between the file param idx and the directory param idx");
        }

        var list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, directoryParamIdx));
        list.add(new VarInsnNode(Opcodes.ALOAD, fileParamIdx));
        list.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/alterorb/launcher/Hook",
                "cacheRedirect",
                Type.getMethodDescriptor(Type.getType(File.class), stringType, stringType)
        ));
        list.add(new InsnNode(Opcodes.ARETURN));

        methodNode.instructions = list;
        methodNode.tryCatchBlocks.clear();
    }

    private int findFileParamIdx(MethodNode methodNode) {
        var hashtable = Type.getType(Hashtable.class).getInternalName();

        for (var insn : methodNode.instructions) {
            if (insn instanceof MethodInsnNode methodInsn
                    && methodInsn.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && methodInsn.owner.equals(hashtable)
                    && methodInsn.name.equals("get")) {

                if (insn.getPrevious() instanceof VarInsnNode varInsn) {
                    return varInsn.var;
                }
            }
        }
        return -1;
    }

    private boolean hasJagexStoreLdc(MethodNode methodNode) {
        for (var insn : methodNode.instructions) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst.equals(JAGEX_STORE)) {
                return true;
            }
        }
        return false;
    }
}
