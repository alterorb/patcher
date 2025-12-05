package net.alterorb.patcher.matcher;

import org.objectweb.asm.tree.AbstractInsnNode;

public record InsnInstructionMatcher(int opcode) implements InstructionMatcher {

    @Override
    public boolean matches(AbstractInsnNode node) {
        return node.getOpcode() == opcode;
    }
}
