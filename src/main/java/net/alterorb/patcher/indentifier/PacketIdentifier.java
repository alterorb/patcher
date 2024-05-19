package net.alterorb.patcher.indentifier;

import net.alterorb.patcher.patcher.Context;
import net.alterorb.patcher.util.ClassStructure;
import net.alterorb.patcher.util.ClassStructurePredicate;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class PacketIdentifier implements Identifier {

    @Override
    public void identify(Context ctx, ClassNode node) {
        var classStructure = ClassStructure.infer(node);

        var predicate = new ClassStructurePredicate()
                .constructorWithArgs(Type.INT_TYPE)
                .constructorWithArgs(Type.getType(byte[].class))
                .instanceFieldCount(2)
                .instanceFieldWithTypeCount(Type.INT_TYPE, 1)
                .instanceFieldWithTypeCount(Type.getType(byte[].class), 1);

        if (predicate.apply(classStructure)) {
            var intFields = classStructure.instanceFieldsWithType(Type.INT_TYPE);
            var byteArrayFields = classStructure.instanceFieldsWithType(Type.getType(byte[].class));

            var offsetField = intFields.getFirst();
            var dataField = byteArrayFields.getFirst();

            ctx.identifyClass("packet", node)
               .identifyField("offset", offsetField)
               .identifyField("data", dataField);
        }
    }
}
