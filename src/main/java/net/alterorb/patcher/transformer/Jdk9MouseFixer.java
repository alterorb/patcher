package net.alterorb.patcher.transformer;

import net.alterorb.patcher.patcher.Context;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static net.alterorb.patcher.util.AsmUtils.findFirstMethodMatching;

/*
 * This transformer fixes mouse right button clicks on jdks verions 9 and above.
 */
public class Jdk9MouseFixer implements Transformer {

    @Override
    public void transform(Context ctx, List<ClassNode> classNodes) {
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        var mousePressed = findFirstMethodMatching(classNode, method -> method.name.equals("mousePressed"));

        if (mousePressed != null) {
            patchMousePressed(mousePressed);
        }
    }

    private void patchMousePressed(MethodNode methodNode) {
        var iterator = methodNode.instructions.iterator();

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();

            if (next instanceof MethodInsnNode methodInsn && methodInsn.name.equals("isMetaDown")) {
                iterator.remove();
                iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "javax/swing/SwingUtilities", "isRightMouseButton", "(Ljava/awt/event/MouseEvent;)Z", false));
            }
        }
    }
}
