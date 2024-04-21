package net.alterorb.patcher.transformer;

import net.alterorb.patcher.AsmUtils;
import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/*
 * This transformer decrypts the ZKM encrypted strings from the classes and removes the decryption methods.
 */
public class ZStringDecrypter implements Transformer {

    private static final Type[] STRING_DECRYPT_ARGUMENTS = {Type.getType(String.class)};
    private static final Type[] CHAR_ARRAY_DECRYPT_ARGUMENTS = {Type.getType(char[].class)};

    @Override
    public void transform(FunOrbGame game, List<ClassNode> classNodes) {
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        var stringDecryptMethod = AsmUtils.findMethodByDescriptor(classNode, Type.getType(char[].class), STRING_DECRYPT_ARGUMENTS);
        var charDecryptMethod = AsmUtils.findMethodByDescriptor(classNode, Type.getType(String.class), CHAR_ARRAY_DECRYPT_ARGUMENTS);

        if (stringDecryptMethod == null) {
            return;
        }
        var decryptKeys = extractDecryptKeys(stringDecryptMethod, charDecryptMethod);
        var methods = classNode.methods;

        methods.forEach(method -> decrypt(method, decryptKeys));
        methods.remove(stringDecryptMethod);
        methods.remove(charDecryptMethod);
    }

    private void decrypt(MethodNode method, DecryptionKeys decryptionKeys) {
        var iterator = method.instructions.iterator();

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();

            if (next instanceof LdcInsnNode ldcInsn
                    && ldcInsn.cst instanceof String strCst
                    && next.getNext() instanceof MethodInsnNode methodInsn
                    && isStringDecryptMethod(methodInsn)) {
                var decryptedChars = decrypt(strCst, decryptionKeys.stringKey);

                ldcInsn.cst = decrypt(decryptedChars, decryptionKeys.charsKey);
                iterator.next();
                iterator.remove(); // z(String) char[] call
                iterator.next();
                iterator.remove(); // z(char[]) String call
            }
        }
    }

    private DecryptionKeys extractDecryptKeys(MethodNode stringDecryptMethod, MethodNode charDecryptMethod) {
        var stringKey = extractStringDecryptKey(stringDecryptMethod);
        var charsKey = extractCharsDecryptKey(charDecryptMethod);

        return new DecryptionKeys(stringKey, charsKey);
    }

    private int extractStringDecryptKey(MethodNode method) {

        for (var insnNode : method.instructions) {
            if (insnNode.getOpcode() == Opcodes.CALOAD) {
                return AsmUtils.extractIntValue(insnNode.getNext());
            }
        }
        return -1;
    }

    private byte[] extractCharsDecryptKey(MethodNode method) {
        var tableSwitchNode = findTableSwitchNode(method);
        var keys = new byte[5];

        if (tableSwitchNode != null) {
            var labels = tableSwitchNode.labels;

            for (int i = 0; i < labels.size(); i++) {
                var abstractInsnNode = labels.get(i).getNext();
                keys[i] = (byte) AsmUtils.extractIntValue(abstractInsnNode);
            }
            keys[4] = (byte) AsmUtils.extractIntValue(tableSwitchNode.dflt.getNext());
            return keys;
        }
        return null;
    }

    private boolean isStringDecryptMethod(MethodInsnNode methodInsnNode) {
        return Objects.equals(methodInsnNode.name, "z")
                && Objects.equals(Type.getReturnType(methodInsnNode.desc), Type.getType(char[].class))
                && Arrays.equals(Type.getArgumentTypes(methodInsnNode.desc), STRING_DECRYPT_ARGUMENTS);
    }

    private TableSwitchInsnNode findTableSwitchNode(MethodNode methodNode) {

        for (var next : methodNode.instructions) {
            if (next instanceof TableSwitchInsnNode tableSwitchNode) {
                return tableSwitchNode;
            }
        }
        return null;
    }

    private char[] decrypt(String encrypted, int key) {
        var chars = encrypted.toCharArray();

        if (chars.length < 2) {
            chars[0] = (char) (chars[0] ^ key);
        }
        return chars;
    }

    private String decrypt(char[] encrypted, byte[] keys) {
        int length = encrypted.length;

        for (var i = 0; length > i; ++i) {
            char aChar = encrypted[i];
            byte key = switch (i % 5) {
                case 0 -> keys[0];
                case 1 -> keys[1];
                case 2 -> keys[2];
                case 3 -> keys[3];
                default -> keys[4];
            };
            encrypted[i] = (char) (aChar ^ key);
        }
        return new String(encrypted);
    }

    private static record DecryptionKeys(int stringKey, byte[] charsKey) {

    }
}
