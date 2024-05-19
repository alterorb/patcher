package net.alterorb.patcher.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassStructure {

    private final ClassNode classNode;
    private final MethodNode classInit;
    private final MethodNode noArgsConstructor;
    private final List<MethodNode> constructors;
    private final List<MethodNode> staticMethods;
    private final List<MethodNode> instanceMethods;
    private final List<FieldNode> staticFields;
    private final List<FieldNode> instanceFields;

    private ClassStructure(
            ClassNode classNode,
            MethodNode classInit,
            MethodNode noArgsConstructor,
            List<MethodNode> constructors,
            List<MethodNode> staticMethods,
            List<MethodNode> instanceMethods,
            List<FieldNode> staticFields,
            List<FieldNode> instanceFields
    ) {
        this.classNode = classNode;
        this.classInit = classInit;
        this.noArgsConstructor = noArgsConstructor;
        this.constructors = constructors;
        this.staticMethods = staticMethods;
        this.instanceMethods = instanceMethods;
        this.staticFields = staticFields;
        this.instanceFields = instanceFields;
    }

    public static ClassStructure infer(ClassNode node) {
        MethodNode classInit = null;
        MethodNode noArgsConstructor = null;
        var constructors = new ArrayList<MethodNode>();

        var instanceMethods = new ArrayList<MethodNode>();
        var staticMethods = new ArrayList<MethodNode>();

        var instanceFields = new ArrayList<FieldNode>();
        var staticFields = new ArrayList<FieldNode>();

        for (MethodNode method : node.methods) {

            if (method.name.equals("<clinit>")) {
                classInit = method;
                continue;
            }
            if (method.name.equals("<init>")) {
                if (Type.getArgumentCount(method.desc) == 0) {
                    if (noArgsConstructor != null) {
                        throw new IllegalStateException("Class '" + node.name + "' has two, no-args, constructors");
                    }
                    noArgsConstructor = method;
                } else {
                    constructors.add(method);
                }
                continue;
            }

            if ((method.access & Opcodes.ACC_STATIC) != 0) {
                staticMethods.add(method);
            } else {
                instanceMethods.add(method);
            }
        }

        for (FieldNode field : node.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) {
                staticFields.add(field);
            } else {
                instanceFields.add(field);
            }
        }
        return new ClassStructure(node, classInit, noArgsConstructor, constructors, staticMethods, instanceMethods, staticFields, instanceFields);
    }

    public MethodNode classInit() {
        return classInit;
    }

    public MethodNode noArgsConstructor() {
        return noArgsConstructor;
    }

    public MethodNode constructorWithArgs(Type... argTypes) {
        for (MethodNode constructor : constructors) {
            var args = Type.getArgumentTypes(constructor.desc);

            if (Arrays.equals(args, argTypes)) {
                return constructor;
            }
        }
        return null;
    }

    public List<FieldNode> instanceFieldsWithType(Type type) {
        return instanceFields.stream()
                             .filter(field -> Type.getType(field.desc).equals(type))
                             .toList();
    }

    public long countInstanceFieldsWithType(Type type) {
        return instanceFields.stream()
                             .filter(field -> Type.getType(field.desc).equals(type))
                             .count();
    }

    public int instanceFieldCount() {
        return instanceFields.size();
    }
}
