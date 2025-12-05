package net.alterorb.patcher.indentifier;

import net.alterorb.patcher.matcher.BytecodePattern;
import net.alterorb.patcher.patcher.IdentifierContext;
import net.alterorb.patcher.util.ClassStructure;
import net.alterorb.patcher.util.ClassStructurePredicate;
import net.alterorb.patcher.util.MethodNodePredicate;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class PacketIdentifier implements Identifier {

    private static final ClassStructurePredicate CLASS_PREDICATE = new ClassStructurePredicate()
            .constructorWithArgs(Type.INT_TYPE)
            .constructorWithArgs(Type.getType(byte[].class))
            .instanceFieldCount(2)
            .instanceFieldWithTypeCount(Type.INT_TYPE, 1)
            .instanceFieldWithTypeCount(Type.getType(byte[].class), 1);

    private static final MethodNodePredicate PPASSWORD_PREDICATE = new MethodNodePredicate()
            .minArgs(1)
            .maxArgs(2)
            .atLeastOneArgWthType(Type.getType(String.class))
            .isFinal()
            .returnType(Type.VOID_TYPE)
            .hasBytecodePatternMatch(BytecodePattern.compile("LLOAD LDC=38L LMUL LSTORE"));

    @Override
    public void identify(IdentifierContext ctx, ClassNode node) {
        var classStructure = ClassStructure.infer(node);

        if (!CLASS_PREDICATE.apply(classStructure)) {
            return;
        }
        var intFields = classStructure.instanceFieldsWithType(Type.INT_TYPE);
        var byteArrayFields = classStructure.instanceFieldsWithType(Type.getType(byte[].class));

        var posField = intFields.getFirst();
        var dataField = byteArrayFields.getFirst();

        var identifiedClass = ctx.identifyClass("packet", node);

        identifiedClass.identifyField("pos", posField)
                       .identifyField("data", dataField)
                       .identifyMethodOrFail("ppassword", this::identifyPPassword);
    }

    private boolean identifyPPassword(MethodNode methodNode) {
        return PPASSWORD_PREDICATE.apply(methodNode);
    }
}
