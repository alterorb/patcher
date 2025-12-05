package net.alterorb.patcher.transformer;

import net.alterorb.patcher.patcher.TransformerContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.nio.charset.StandardCharsets;
import java.util.List;

/*
 * This transformer replaces the original ppassword function with a custom, null-terminated, utf-8 encoded string.
 */
public class ComplexPasswords implements Transformer {

    @Override
    public void transform(TransformerContext ctx, List<ClassNode> classNodes) {
        var packetClass = ctx.identifiedClass("packet");
        var dataField = packetClass.field("data");
        var posField = packetClass.field("pos");
        var passwordMethod = packetClass.method("ppassword");

        patchMethod(packetClass.node(), passwordMethod, dataField, posField);
    }

    private void patchMethod(ClassNode packetClass, MethodNode method, FieldNode dataField, FieldNode posField) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();

        var argumentTypes = Type.getArgumentTypes(method.desc);
        var stringArgIdx = 0;

        for (int i = 0; i < argumentTypes.length; i++) {
            Type argumentType = argumentTypes[i];
            if (argumentType.equals(Type.getType(String.class))) {
                stringArgIdx = i;
                break;
            }
        }

        // public void pjstr(String value) {
        //     byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        //     System.arraycopy(bytes, 0, this.data, this.pos, bytes.length);
        //     this.pos += bytes.length;
        //     this.data[this.pos++] = 0;
        // }
        method.visitVarInsn(Opcodes.ALOAD, stringArgIdx);
        method.visitFieldInsn(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B", false);
        method.visitVarInsn(Opcodes.ASTORE, 2);
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, packetClass.name, dataField.name, "[B");
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, packetClass.name, posField.name, "I");
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitInsn(Opcodes.ARRAYLENGTH);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.DUP);
        method.visitFieldInsn(Opcodes.GETFIELD, packetClass.name, posField.name, "I");
        method.visitVarInsn(Opcodes.ALOAD, 2);
        method.visitInsn(Opcodes.ARRAYLENGTH);
        method.visitInsn(Opcodes.IADD);
        method.visitFieldInsn(Opcodes.PUTFIELD, packetClass.name, posField.name, "I");
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitFieldInsn(Opcodes.GETFIELD, packetClass.name, dataField.name, "[B");
        method.visitVarInsn(Opcodes.ALOAD, 0);
        method.visitInsn(Opcodes.DUP);
        method.visitFieldInsn(Opcodes.GETFIELD, packetClass.name, posField.name, "I");
        method.visitInsn(Opcodes.DUP_X1);
        method.visitInsn(Opcodes.ICONST_1);
        method.visitInsn(Opcodes.IADD);
        method.visitFieldInsn(Opcodes.PUTFIELD, packetClass.name, "pos", "I");
        method.visitIntInsn(Opcodes.BIPUSH, 0);
        method.visitInsn(Opcodes.BASTORE);
        method.visitInsn(Opcodes.RETURN);
    }
}
