package net.alterorb.patcher.matcher;

import org.objectweb.asm.tree.AbstractInsnNode;

public sealed interface InstructionMatcher permits InsnInstructionMatcher, LdcInstructionMatcher {

    boolean matches(AbstractInsnNode node);
}
