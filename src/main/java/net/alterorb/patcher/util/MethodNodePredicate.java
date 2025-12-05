package net.alterorb.patcher.util;

import net.alterorb.patcher.matcher.BytecodePattern;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.function.Predicate;

public class MethodNodePredicate {

    private Predicate<MethodNode> predicate = structure -> true;

    public MethodNodePredicate minArgs(int minArgs) {
        predicate = predicate.and(methodNode -> Type.getArgumentCount(methodNode.desc) >= minArgs);
        return this;
    }

    public MethodNodePredicate maxArgs(int maxArgs) {
        predicate = predicate.and(methodNode -> Type.getArgumentCount(methodNode.desc) <= maxArgs);
        return this;
    }

    public MethodNodePredicate exactArgs(int args) {
        predicate = predicate.and(methodNode -> Type.getArgumentCount(methodNode.desc) == args);
        return this;
    }

    public MethodNodePredicate returnType(Type returnType) {
        predicate = predicate.and(methodNode -> Type.getReturnType(methodNode.desc).equals(returnType));
        return this;
    }

    public MethodNodePredicate atLeastOneArgWthType(Type argType) {
        predicate = predicate.and(methodNode -> {
            var argumentTypes = Type.getArgumentTypes(methodNode.desc);
            for (var methodArgType : argumentTypes) {
                if (methodArgType.equals(argType)) {
                    return true;
                }
            }
            return false;
        });
        return this;
    }

    public MethodNodePredicate isPrivate() {
        predicate = predicate.and(methodNode -> (methodNode.access & Opcodes.ACC_PRIVATE) != 0);
        return this;
    }

    public MethodNodePredicate isPublic() {
        predicate = predicate.and(methodNode -> (methodNode.access & Opcodes.ACC_PUBLIC) != 0);
        return this;
    }

    public MethodNodePredicate isFinal() {
        predicate = predicate.and(methodNode -> (methodNode.access & Opcodes.ACC_FINAL) != 0);
        return this;
    }

    public MethodNodePredicate isStatic() {
        predicate = predicate.and(methodNode -> (methodNode.access & Opcodes.ACC_STATIC) != 0);
        return this;
    }

    public MethodNodePredicate hasBytecodePatternMatch(BytecodePattern pattern) {
        predicate = predicate.and(methodNode -> pattern.match(methodNode.instructions));
        return this;
    }

    public MethodNodePredicate hasBytecodePatternMatchCount(BytecodePattern pattern, int count) {
        predicate = predicate.and(methodNode -> pattern.countMatches(methodNode.instructions) == count);
        return this;
    }

    public boolean apply(MethodNode structure) {
        return predicate.test(structure);
    }
}
