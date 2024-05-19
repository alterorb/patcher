package net.alterorb.patcher.transformer;

import net.alterorb.patcher.patcher.Context;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface Transformer {

    void transform(Context ctx, List<ClassNode> classNodes);
}
