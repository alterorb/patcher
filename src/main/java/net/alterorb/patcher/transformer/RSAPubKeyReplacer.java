package net.alterorb.patcher.transformer;

import net.alterorb.patcher.patcher.Context;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;

import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import static net.alterorb.patcher.util.AsmUtils.findClinit;

/*
 * This key replaces the RSA public key that is used to encrypt packets.
 */
public class RSAPubKeyReplacer implements Transformer {

    private final RSAPublicKeySpec original;
    private final RSAPublicKeySpec replacement;

    public RSAPubKeyReplacer(RSAPublicKeySpec original, RSAPublicKeySpec replacement) {
        this.original = original;
        this.replacement = replacement;
    }

    @Override
    public void transform(Context ctx, List<ClassNode> classNodes) {
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        var clinit = findClinit(classNode);

        if (clinit == null) {
            return;
        }
        var iterator = clinit.instructions.iterator();

        var originalModulus = original.getModulus().toString();
        var originalExponent = original.getPublicExponent().toString();

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();

            if (next instanceof LdcInsnNode ldcInsn) {

                if (ldcInsn.cst.equals(originalModulus)) {
                    ldcInsn.cst = replacement.getModulus().toString();
                }

                if (ldcInsn.cst.equals(originalExponent)) {
                    ldcInsn.cst = replacement.getPublicExponent().toString();
                }
            }
        }
    }
}
