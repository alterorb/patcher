package net.alterorb.patcher.matcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
class BytecodePatternTest {

    @Test
    void testSingleInstructionMatch() {
        var instructions = new InsnList();
        instructions.add(new InsnNode(Opcodes.NOP));

        var pattern = BytecodePattern.compile("NOP");
        boolean result = pattern.match(instructions);

        assertTrue(result, "The pattern 'NOP' should match a single NOP instruction.");
    }

    @Test
    void testMultipleInstructionMatch() {
        var instructions = new InsnList();
        instructions.add(new InsnNode(Opcodes.ICONST_1));
        instructions.add(new InsnNode(Opcodes.ICONST_2));
        instructions.add(new InsnNode(Opcodes.IADD));

        var pattern = BytecodePattern.compile("ICONST_1 ICONST_2 IADD");

        assertTrue(pattern.match(instructions));
    }

    @Test
    void testPatternMismatch() {
        var instructions = new InsnList();
        instructions.add(new InsnNode(Opcodes.ICONST_1));
        instructions.add(new InsnNode(Opcodes.ICONST_2));
        instructions.add(new InsnNode(Opcodes.IADD));

        var pattern = BytecodePattern.compile("ICONST_1 ICONST_2 ISUB");

        assertFalse(pattern.match(instructions));
    }

    @Test
    void testEmptyInstructionListDoesNotMatch() {
        var instructions = new InsnList();

        var pattern = BytecodePattern.compile("ICONST_1");

        assertFalse(pattern.match(instructions));
    }

    @Test
    void testLdcInstructionMatchWithString() {
        var instructions = new InsnList();
        instructions.add(new LdcInsnNode("Hello"));

        var pattern = BytecodePattern.compile("""
                LDC="Hello"
                """);
        assertTrue(pattern.match(instructions));
    }

    @Test
    void testLdcInstructionBadString() {
        var instructions = new InsnList();
        instructions.add(new LdcInsnNode("Hello"));

        assertThrows(IllegalArgumentException.class, () -> {
            BytecodePattern.compile("""
                    LDC="Hello
                    """);
        });
    }

    @Test
    void testLdcInstructionMatchWithLong() {
        var instructions = new InsnList();
        instructions.add(new LdcInsnNode(105035L));

        var pattern = BytecodePattern.compile("""
                LDC=105035L
                """);
        assertTrue(pattern.match(instructions));
    }

    @Test
    void testLdcInstructionMatchWithInt() {
        var instructions = new InsnList();
        instructions.add(new LdcInsnNode(1235));

        var pattern = BytecodePattern.compile("""
                LDC=1235
                """);
        assertTrue(pattern.match(instructions));
    }

    @Test
    void testLdcInstructionMatchAny() {
        var instructions = new InsnList();
        instructions.add(new LdcInsnNode("Beep"));

        var pattern = BytecodePattern.compile("LDC");
        assertTrue(pattern.match(instructions));
    }

    @Test
    void testMismatchedLdcInstructionWithString() {
        var instructions = new InsnList();
        instructions.add(new InsnNode(Opcodes.LDC));

        var pattern = BytecodePattern.compile("""
                LDC="World"
                """);
        assertFalse(pattern.match(instructions));
    }

    @Test
    void testCountMatches() {
        var instructions = new InsnList();
        instructions.add(new InsnNode(Opcodes.ICONST_1));
        instructions.add(new InsnNode(Opcodes.ICONST_2));
        instructions.add(new InsnNode(Opcodes.IADD));

        instructions.add(new InsnNode(Opcodes.NOP));

        instructions.add(new InsnNode(Opcodes.ICONST_1));
        instructions.add(new InsnNode(Opcodes.ICONST_2));
        instructions.add(new InsnNode(Opcodes.IADD));

        var pattern = BytecodePattern.compile("ICONST_1 ICONST_2 IADD");

        assertEquals(2, pattern.countMatches(instructions));
    }
}