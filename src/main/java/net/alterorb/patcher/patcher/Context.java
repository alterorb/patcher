package net.alterorb.patcher.patcher;

import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Context {

    private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private final Map<String, IdentifiedClass> identifiedClasses = new HashMap<>();
    private final Map<String, IdentifiedStaticField> identifiedStaticFields = new HashMap<>();

    private final FunOrbGame game;

    public Context(FunOrbGame game) {
        this.game = game;
    }

    public FunOrbGame game() {
        return game;
    }

    public void identifyStaticField(String name, ClassNode owner, FieldNode fieldNode) {
        var existingStaticField = identifiedStaticFields.get(name);

        if (existingStaticField != null) {
            var existingOwner = existingStaticField.owner.name;
            var existingField = existingStaticField.field;
            throw new IllegalStateException("The static field " + existingOwner + "." + existingField.name + "(" + existingField.desc + ") has already been identified with the name '" + name + "'");
        }
        LOGGER.debug("Identified static field '{}.{}({})' as '{}'", owner.name, fieldNode.name, fieldNode.desc, name);
        identifiedStaticFields.put(name, new IdentifiedStaticField(name, owner, fieldNode));
    }

    public IdentifiedClass identifiedClass(String name) {
        var identifiedClass = identifiedClasses.get(name);

        if (identifiedClass == null) {
            throw new IllegalStateException("No class has been identified with the name '" + name + "'");
        }
        return identifiedClass;
    }

    public IdentifiedClass identifyClass(String name, ClassNode node) {
        var existingClass = identifiedClasses.get(name);

        if (existingClass != null) {
            throw new IllegalStateException("The class " + existingClass.node.name + " has already been identified with the name '" + name + "'");
        }
        var identifiedClass = new IdentifiedClass(name, node);
        identifiedClasses.put(name, identifiedClass);
        LOGGER.debug("Identified class '{}' as '{}'", node.name, name);
        return identifiedClass;
    }

    public record IdentifiedClass(String name, ClassNode node, Map<String, FieldNode> fields, Map<String, MethodNode> methods) {

        public IdentifiedClass(String name, ClassNode node) {
            this(name, node, new HashMap<>(), new HashMap<>());
        }

        public IdentifiedClass identifyField(String name, FieldNode fieldNode) {
            fields.put(name, fieldNode);
            LOGGER.debug("Identified field '{}({})' as '{}'", fieldNode.name, fieldNode.desc, name);
            return this;
        }

        public IdentifiedClass identifyMethod(String name, MethodNode methodNode) {
            methods.put(name, methodNode);
            LOGGER.debug("Identified method '{}{}' as '{}'", methodNode.name, methodNode.desc, name);
            return this;
        }

        public String className() {
            return node.name;
        }
    }

    public record IdentifiedStaticField(String name, ClassNode owner, FieldNode field) {

    }
}
