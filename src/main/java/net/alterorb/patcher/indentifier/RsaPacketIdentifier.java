package net.alterorb.patcher.indentifier;

import net.alterorb.patcher.patcher.Context;
import net.alterorb.patcher.util.ClassStructure;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static net.alterorb.patcher.util.AsmUtils.extractIntValue;
import static net.alterorb.patcher.util.AsmUtils.findField;

@DependsOn(PacketIdentifier.class)
public class RsaPacketIdentifier implements Identifier {

    @Override
    public void identify(Context ctx, ClassNode node) {
        var classStructure = ClassStructure.infer(node);
        var clinit = classStructure.classInit();

        if (clinit == null) {
            return;
        }
        var identifiedClass = ctx.identifiedClass("packet");

        // sipush 256
        // invokespecial ec.<init>(I)V
        // putstatic ta.c:ec - rsaPacket

        for (AbstractInsnNode insn : clinit.instructions) {
            // invokespecial ec.<init>(I)V
            if (insn instanceof MethodInsnNode methodInsn
                    && methodInsn.owner.equals(identifiedClass.className())
                    && methodInsn.name.equals("<init>")
                    && methodInsn.desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE))
            ) {

                // sipush 256
                if (insn.getPrevious() instanceof IntInsnNode intInsn && extractIntValue(intInsn) == 256) {
                    // putstatic ta.c:ec - rsaPacket
                    var fieldInsn = (FieldInsnNode) insn.getNext();
                    var rsaPacketField = findField(node, field -> field.name.equals(fieldInsn.name) && field.desc.equals(fieldInsn.desc));

                    if (rsaPacketField == null) {
                        throw new IllegalStateException("failed to find putstatic reference");
                    }
                    ctx.identifyStaticField("rsaPacket", node, rsaPacketField);
                    break;
                }
            }
        }
    }
}
