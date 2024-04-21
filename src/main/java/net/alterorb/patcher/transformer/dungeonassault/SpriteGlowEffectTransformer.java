package net.alterorb.patcher.transformer.dungeonassault;

import net.alterorb.patcher.FunOrbGame;
import net.alterorb.patcher.transformer.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

import static net.alterorb.patcher.AsmUtils.findMethodByDescriptor;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;

/*
 * Fixes the glitchy Dungeon Assault sprite glow effect on modern java versions.
 */
public class SpriteGlowEffectTransformer implements Transformer {

    private static final List<String> CLASSES_TO_TRANSFORM = List.of("gd", "pp");

    @Override
    public void transform(FunOrbGame game, List<ClassNode> classNodes) {
        if (game != FunOrbGame.DUNGEON_ASSAULT) {
            return;
        }
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        if (!CLASSES_TO_TRANSFORM.contains(classNode.name)) {
            return;
        }
        var drawMethod = findMethodByDescriptor(classNode, VOID_TYPE, INT_TYPE, INT_TYPE, INT_TYPE);

        if (drawMethod == null) {
            throw new IllegalStateException("Could not locate the draw method in the targeted class");
        }
        for (AbstractInsnNode insn : drawMethod.instructions) {
            if (insn instanceof MethodInsnNode methodInsn && methodInsn.name.equals("hashCode")) {
                drawMethod.instructions.insert(insn, new InsnNode(Opcodes.ICONST_1));
                drawMethod.instructions.remove(insn.getPrevious()); // aload 0
                drawMethod.instructions.remove(insn); // invokevirtual java/lang/Object.hashCode()I
            }
        }
    }
}
