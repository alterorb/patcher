package net.alterorb.patcher.matcher;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * Represents a LDC instruction to be matched. The constant value may be `null` to represent `any` constant value.
 * @param constant The constant to be matched against.
 */
public record LdcInstructionMatcher(Object constant) implements InstructionMatcher {

    @Override
    public boolean matches(AbstractInsnNode node) {
        return node instanceof LdcInsnNode ldcInsnNode && (constant == null || ldcInsnNode.cst.equals(constant));
    }
}
