package net.alterorb.patcher.transformer;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface Transformer {

    void transform(List<ClassNode> classNodes);
}
