package net.alterorb.patcher.transformer;

import net.alterorb.patcher.AsmUtils;
import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/*
 * This transformer should be run after strings have already been decrypted, it performs the following transforms:
 * - Removes the zkm string array initialization
 * - Removes the zkm string array from the class
 * - Removes references to the zkm string array in the class node and replaces with LDCs
 */
public class ZStringArrayInliner implements Transformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZStringArrayInliner.class);

    private static final Predicate<FieldNode> ZKM_STRING_ARRAY =
            fieldNode -> fieldNode.access == (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)
                    && fieldNode.desc.equals("[Ljava/lang/String;");

    @Override
    public void transform(FunOrbGame game, List<ClassNode> classNodes) {
        classNodes.forEach(this::transform);
    }

    private void transform(ClassNode classNode) {
        var stringArrayField = AsmUtils.findField(classNode, ZKM_STRING_ARRAY);

        if (stringArrayField == null) {
            return;
        }
        LOGGER.debug("Processing class '{}'", classNode.name);
        Predicate<FieldInsnNode> predicate = fieldInsn -> Objects.equals(fieldInsn.owner, classNode.name)
                && Objects.equals(fieldInsn.name, stringArrayField.name)
                && Objects.equals(fieldInsn.desc, "[Ljava/lang/String;");

        LOGGER.trace("stringArrayField={}", stringArrayField.name);
        var clinit = AsmUtils.findClinit(classNode);
        var constants = extractConstants(clinit, predicate);

        LOGGER.trace("clinit constants={}", Arrays.toString(constants));
        clearClinit(clinit, predicate);

        classNode.methods.forEach(method -> inlineStrings(method, predicate, constants));
        classNode.fields.remove(stringArrayField);
        LOGGER.debug("Finished processing class '{}'", classNode.name);
    }

    private Object[] extractConstants(MethodNode clinit, Predicate<FieldInsnNode> zStrArrayPredicate) {
        // The string array initialization is always the first instruction block in the clinit
        var iterator = clinit.instructions.iterator();
        // This is guaranteed safe because this code only runs if we find a zkm string array
        var arraySize = AsmUtils.extractIntValue(iterator.next());
        var constants = new Object[arraySize];

        while (iterator.hasNext()) {
            var next = iterator.next();

            // Iterate until we find a PUTSTATIC into the zkm string array
            if (next instanceof FieldInsnNode fieldInsn && next.getOpcode() == Opcodes.PUTSTATIC && zStrArrayPredicate.test(fieldInsn)) {
                break;
            }

            if (next instanceof LdcInsnNode ldcInsn) {
                var idxInsn = AsmUtils.extractIntValue(ldcInsn.getPrevious());
                constants[idxInsn] = ldcInsn.cst;
            }
        }
        return constants;
    }

    private void clearClinit(MethodNode clinit, Predicate<FieldInsnNode> zStrArrayPredicate) {
        var iterator = clinit.instructions.iterator();

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();
            iterator.remove();

            // Iterate until we find a PUTSTATIC into the zkm string array
            if (next instanceof FieldInsnNode fieldInsn && next.getOpcode() == Opcodes.PUTSTATIC && zStrArrayPredicate.test(fieldInsn)) {
                break;
            }
        }
    }

    private void inlineStrings(MethodNode method, Predicate<FieldInsnNode> zStrArrayPredicate, Object[] constants) {
        var iterator = method.instructions.iterator();
        LOGGER.trace("Inlining strings in method {}.{}", method.name, method.desc);

        while (iterator.hasNext()) {
            AbstractInsnNode next = iterator.next();

            if (!(next instanceof FieldInsnNode fieldInsn) || fieldInsn.getOpcode() != Opcodes.GETSTATIC || !zStrArrayPredicate.test(fieldInsn)) {
                continue;
            }
            /* Replaces the instruction set below with a LDC
             * getstatic a.z [Ljava/lang/String;
             * bipush index
             * aaload
             */
            iterator.remove(); // GETSTATIC
            var bipushInsn = iterator.next();
            int idx = AsmUtils.extractIntValue(bipushInsn);
            iterator.remove(); // BIPUSH
            iterator.next();
            iterator.remove(); // AALOAD
            iterator.add(new LdcInsnNode(constants[idx]));
        }
    }
}
