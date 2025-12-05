package net.alterorb.patcher.matcher;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;

public final class BytecodePattern {

    private final InstructionMatcher[] instructionMatchers;

    private BytecodePattern(InstructionMatcher[] instructionMatchers) {
        this.instructionMatchers = instructionMatchers;
    }

    public static BytecodePattern compile(String pattern) {

        if (pattern.isBlank()) {
            throw new IllegalArgumentException("pattern must not be blank");
        }
        var tokens = pattern.trim().split("\\s+");
        var tokenMatchers = new InstructionMatcher[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            tokenMatchers[i] = parseToken(tokens[i]);
        }

        return new BytecodePattern(tokenMatchers);
    }

    private static InstructionMatcher parseToken(String token) {
        if (token.startsWith("LDC")) {
            return parseLdcToken(token);
        } else {
            return new InsnInstructionMatcher(nameToOpcode(token));
        }
    }

    private static LdcInstructionMatcher parseLdcToken(String token) {
        var equalsIdx = token.indexOf('=');

        if (equalsIdx != -1) {
            var value = token.substring(equalsIdx + 1);
            return new LdcInstructionMatcher(parseLdcValue(value));
        }
        return new LdcInstructionMatcher(null);
    }

    private static Object parseLdcValue(String value) {
        if (value.charAt(0) == '"') {
            if (value.charAt(value.length() - 1) != '"') {
                throw new IllegalArgumentException("Unexpected end of string literal: " + value);
            }
            return value.substring(1, value.length() - 1);
        } else {
            if (value.charAt(value.length() - 1) == 'L') {
                return Long.parseLong(value.substring(0, value.length() - 1));
            } else {
                return Integer.parseInt(value);
            }
        }
    }

    private static int nameToOpcode(String opcodeName) {
        return switch (opcodeName) {
            case "NOP" -> Opcodes.NOP;
            case "ACONST_NULL" -> Opcodes.ACONST_NULL;
            case "ICONST_M1" -> Opcodes.ICONST_M1;
            case "ICONST_0" -> Opcodes.ICONST_0;
            case "ICONST_1" -> Opcodes.ICONST_1;
            case "ICONST_2" -> Opcodes.ICONST_2;
            case "ICONST_3" -> Opcodes.ICONST_3;
            case "ICONST_4" -> Opcodes.ICONST_4;
            case "ICONST_5" -> Opcodes.ICONST_5;
            case "LCONST_0" -> Opcodes.LCONST_0;
            case "LCONST_1" -> Opcodes.LCONST_1;
            case "FCONST_0" -> Opcodes.FCONST_0;
            case "FCONST_1" -> Opcodes.FCONST_1;
            case "FCONST_2" -> Opcodes.FCONST_2;
            case "DCONST_0" -> Opcodes.DCONST_0;
            case "DCONST_1" -> Opcodes.DCONST_1;
            case "BIPUSH" -> Opcodes.BIPUSH;
            case "SIPUSH" -> Opcodes.SIPUSH;
            case "LDC" -> Opcodes.LDC;
            case "ILOAD" -> Opcodes.ILOAD;
            case "LLOAD" -> Opcodes.LLOAD;
            case "FLOAD" -> Opcodes.FLOAD;
            case "DLOAD" -> Opcodes.DLOAD;
            case "ALOAD" -> Opcodes.ALOAD;
            case "IALOAD" -> Opcodes.IALOAD;
            case "LALOAD" -> Opcodes.LALOAD;
            case "FALOAD" -> Opcodes.FALOAD;
            case "DALOAD" -> Opcodes.DALOAD;
            case "AALOAD" -> Opcodes.AALOAD;
            case "BALOAD" -> Opcodes.BALOAD;
            case "CALOAD" -> Opcodes.CALOAD;
            case "SALOAD" -> Opcodes.SALOAD;
            case "ISTORE" -> Opcodes.ISTORE;
            case "LSTORE" -> Opcodes.LSTORE;
            case "FSTORE" -> Opcodes.FSTORE;
            case "DSTORE" -> Opcodes.DSTORE;
            case "ASTORE" -> Opcodes.ASTORE;
            case "IASTORE" -> Opcodes.IASTORE;
            case "LASTORE" -> Opcodes.LASTORE;
            case "FASTORE" -> Opcodes.FASTORE;
            case "DASTORE" -> Opcodes.DASTORE;
            case "AASTORE" -> Opcodes.AASTORE;
            case "BASTORE" -> Opcodes.BASTORE;
            case "CASTORE" -> Opcodes.CASTORE;
            case "SASTORE" -> Opcodes.SASTORE;
            case "POP" -> Opcodes.POP;
            case "POP2" -> Opcodes.POP2;
            case "DUP" -> Opcodes.DUP;
            case "DUP_X1" -> Opcodes.DUP_X1;
            case "DUP_X2" -> Opcodes.DUP_X2;
            case "DUP2" -> Opcodes.DUP2;
            case "DUP2_X1" -> Opcodes.DUP2_X1;
            case "DUP2_X2" -> Opcodes.DUP2_X2;
            case "SWAP" -> Opcodes.SWAP;
            case "IADD" -> Opcodes.IADD;
            case "LADD" -> Opcodes.LADD;
            case "FADD" -> Opcodes.FADD;
            case "DADD" -> Opcodes.DADD;
            case "ISUB" -> Opcodes.ISUB;
            case "LSUB" -> Opcodes.LSUB;
            case "FSUB" -> Opcodes.FSUB;
            case "DSUB" -> Opcodes.DSUB;
            case "IMUL" -> Opcodes.IMUL;
            case "LMUL" -> Opcodes.LMUL;
            case "FMUL" -> Opcodes.FMUL;
            case "DMUL" -> Opcodes.DMUL;
            case "IDIV" -> Opcodes.IDIV;
            case "LDIV" -> Opcodes.LDIV;
            case "FDIV" -> Opcodes.FDIV;
            case "DDIV" -> Opcodes.DDIV;
            case "IREM" -> Opcodes.IREM;
            case "LREM" -> Opcodes.LREM;
            case "FREM" -> Opcodes.FREM;
            case "DREM" -> Opcodes.DREM;
            case "INEG" -> Opcodes.INEG;
            case "LNEG" -> Opcodes.LNEG;
            case "FNEG" -> Opcodes.FNEG;
            case "DNEG" -> Opcodes.DNEG;
            case "ISHL" -> Opcodes.ISHL;
            case "LSHL" -> Opcodes.LSHL;
            case "ISHR" -> Opcodes.ISHR;
            case "LSHR" -> Opcodes.LSHR;
            case "IUSHR" -> Opcodes.IUSHR;
            case "LUSHR" -> Opcodes.LUSHR;
            case "IAND" -> Opcodes.IAND;
            case "LAND" -> Opcodes.LAND;
            case "IOR" -> Opcodes.IOR;
            case "LOR" -> Opcodes.LOR;
            case "IXOR" -> Opcodes.IXOR;
            case "LXOR" -> Opcodes.LXOR;
            case "IINC" -> Opcodes.IINC;
            case "I2L" -> Opcodes.I2L;
            case "I2F" -> Opcodes.I2F;
            case "I2D" -> Opcodes.I2D;
            case "L2I" -> Opcodes.L2I;
            case "L2F" -> Opcodes.L2F;
            case "L2D" -> Opcodes.L2D;
            case "F2I" -> Opcodes.F2I;
            case "F2L" -> Opcodes.F2L;
            case "F2D" -> Opcodes.F2D;
            case "D2I" -> Opcodes.D2I;
            case "D2L" -> Opcodes.D2L;
            case "D2F" -> Opcodes.D2F;
            case "I2B" -> Opcodes.I2B;
            case "I2C" -> Opcodes.I2C;
            case "I2S" -> Opcodes.I2S;
            case "LCMP" -> Opcodes.LCMP;
            case "FCMPL" -> Opcodes.FCMPL;
            case "FCMPG" -> Opcodes.FCMPG;
            case "DCMPL" -> Opcodes.DCMPL;
            case "DCMPG" -> Opcodes.DCMPG;
            case "IFEQ" -> Opcodes.IFEQ;
            case "IFNE" -> Opcodes.IFNE;
            case "IFLT" -> Opcodes.IFLT;
            case "IFGE" -> Opcodes.IFGE;
            case "IFGT" -> Opcodes.IFGT;
            case "IFLE" -> Opcodes.IFLE;
            case "IF_ICMPEQ" -> Opcodes.IF_ICMPEQ;
            case "IF_ICMPNE" -> Opcodes.IF_ICMPNE;
            case "IF_ICMPLT" -> Opcodes.IF_ICMPLT;
            case "IF_ICMPGE" -> Opcodes.IF_ICMPGE;
            case "IF_ICMPGT" -> Opcodes.IF_ICMPGT;
            case "IF_ICMPLE" -> Opcodes.IF_ICMPLE;
            case "IF_ACMPEQ" -> Opcodes.IF_ACMPEQ;
            case "IF_ACMPNE" -> Opcodes.IF_ACMPNE;
            case "GOTO" -> Opcodes.GOTO;
            case "JSR" -> Opcodes.JSR;
            case "RET" -> Opcodes.RET;
            case "TABLESWITCH" -> Opcodes.TABLESWITCH;
            case "LOOKUPSWITCH" -> Opcodes.LOOKUPSWITCH;
            case "IRETURN" -> Opcodes.IRETURN;
            case "LRETURN" -> Opcodes.LRETURN;
            case "FRETURN" -> Opcodes.FRETURN;
            case "DRETURN" -> Opcodes.DRETURN;
            case "ARETURN" -> Opcodes.ARETURN;
            case "RETURN" -> Opcodes.RETURN;
            case "GETSTATIC" -> Opcodes.GETSTATIC;
            case "PUTSTATIC" -> Opcodes.PUTSTATIC;
            case "GETFIELD" -> Opcodes.GETFIELD;
            case "PUTFIELD" -> Opcodes.PUTFIELD;
            case "INVOKEVIRTUAL" -> Opcodes.INVOKEVIRTUAL;
            case "INVOKESPECIAL" -> Opcodes.INVOKESPECIAL;
            case "INVOKESTATIC" -> Opcodes.INVOKESTATIC;
            case "INVOKEINTERFACE" -> Opcodes.INVOKEINTERFACE;
            case "INVOKEDYNAMIC" -> Opcodes.INVOKEDYNAMIC;
            case "NEW" -> Opcodes.NEW;
            case "NEWARRAY" -> Opcodes.NEWARRAY;
            case "ANEWARRAY" -> Opcodes.ANEWARRAY;
            case "ARRAYLENGTH" -> Opcodes.ARRAYLENGTH;
            case "ATHROW" -> Opcodes.ATHROW;
            case "CHECKCAST" -> Opcodes.CHECKCAST;
            case "INSTANCEOF" -> Opcodes.INSTANCEOF;
            case "MONITORENTER" -> Opcodes.MONITORENTER;
            case "MONITOREXIT" -> Opcodes.MONITOREXIT;
            case "MULTIANEWARRAY" -> Opcodes.MULTIANEWARRAY;
            case "IFNULL" -> Opcodes.IFNULL;
            case "IFNONNULL" -> Opcodes.IFNONNULL;
            default -> throw new IllegalArgumentException("Unknown/unsupported opcode name: " + opcodeName);
        };
    }

    public boolean match(InsnList insnList) {
        int matcherIdx = 0;
        for (var abstractInsnNode : insnList) {
            if (instructionMatchers[matcherIdx].matches(abstractInsnNode)) {
                matcherIdx++;
                if (matcherIdx == instructionMatchers.length) {
                    return true;
                }
            } else {
                matcherIdx = 0;
            }
        }
        return false;
    }

    public int countMatches(InsnList insnList) {
        int count = 0;
        int matcherIdx = 0;

        for (var abstractInsnNode : insnList) {
            if (instructionMatchers[matcherIdx].matches(abstractInsnNode)) {
                matcherIdx++;
                if (matcherIdx == instructionMatchers.length) {
                    matcherIdx = 0;
                    count++;
                }
            } else {
                matcherIdx = 0;
            }
        }
        return count;
    }
}
