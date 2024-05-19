package net.alterorb.patcher.util;

import org.objectweb.asm.Type;

import java.util.function.Predicate;

public class ClassStructurePredicate {

    private Predicate<ClassStructure> predicate = structure -> true;

    public ClassStructurePredicate noArgsConstructor() {
        predicate = predicate.and(structure -> structure.noArgsConstructor() != null);
        return this;
    }

    public ClassStructurePredicate constructorWithArgs(Type... args) {
        predicate = predicate.and(structure -> structure.constructorWithArgs(args) != null);
        return this;
    }

    public ClassStructurePredicate instanceFieldCount(int count) {
        predicate = predicate.and(structure -> structure.instanceFieldCount() == count);
        return this;
    }

    public ClassStructurePredicate instanceFieldWithTypeCount(Type type, int count) {
        predicate = predicate.and(structure -> structure.countInstanceFieldsWithType(type) == count);
        return this;
    }

    public boolean apply(ClassStructure structure) {
        return predicate.test(structure);
    }
}
