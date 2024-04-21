package net.alterorb.patcher.transformer;

import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Objects;

/*
 * This transformer patches the gameshell.checkhost method to accept any domain.
 * It requires strings to have already been decrypted to work.
 */
public class CheckhostTransformer implements Transformer {

    @Override
    public void transform(FunOrbGame game, List<ClassNode> classNodes) {
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        classNode.methods.stream()
                         .filter(this::isCheckHost)
                         .findFirst()
                         .ifPresent(this::patchCheckHost);
    }

    private boolean isCheckHost(MethodNode method) {
        for (var insn : method.instructions) {
            if (insn instanceof LdcInsnNode ldc && Objects.equals(ldc.cst, ".funorb.com")) {
                return true;
            }
        }
        return false;
    }

    private void patchCheckHost(MethodNode method) {
        InsnList insnList = new InsnList();
        insnList.add(new InsnNode(Opcodes.ICONST_1));
        insnList.add(new InsnNode(Opcodes.IRETURN));

        method.instructions = insnList;
        method.tryCatchBlocks.clear();
    }
}
