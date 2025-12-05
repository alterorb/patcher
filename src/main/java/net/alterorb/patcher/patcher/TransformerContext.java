package net.alterorb.patcher.patcher;

import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransformerContext {

    private final FunOrbGame game;
    private final Map<String, IdentifiedClass> classes;

    public TransformerContext(IdentifierContext context) {
        this.game = context.game;
        this.classes = new LinkedHashMap<>();

        for (var identifiedClass : context.identifiedClasses.entrySet()) {
            this.classes.put(identifiedClass.getKey(), new IdentifiedClass(identifiedClass.getValue()));
        }
    }

    public FunOrbGame game() {
        return game;
    }

    public IdentifiedClass identifiedClass(String name) {
        var identifiedClass = classes.get(name);
        if (identifiedClass == null) {
            throw new IllegalArgumentException("No class was identified with the name '" + name + "'");
        }
        return identifiedClass;
    }

    public record IdentifiedClass(String name, ClassNode node, Map<String, FieldNode> fields, Map<String, MethodNode> methods) {

        private IdentifiedClass(IdentifierContext.IdentifiedClass identifiedClass) {
            this(identifiedClass.name(), identifiedClass.node(), identifiedClass.fields(), identifiedClass.methods());
        }

        public FieldNode field(String name) {
            var field = fields.get(name);
            if (field == null) {
                throw new IllegalArgumentException("No field was identified with the name '" + name + "'");
            }
            return field;
        }

        public MethodNode method(String name) {
            var method = methods.get(name);
            if (method == null) {
                throw new IllegalArgumentException("No method was identified with the name '" + name + "'");
            }
            return method;
        }
    }
}
