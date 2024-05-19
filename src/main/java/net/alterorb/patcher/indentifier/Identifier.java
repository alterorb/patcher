package net.alterorb.patcher.indentifier;

import net.alterorb.patcher.patcher.Context;
import org.objectweb.asm.tree.ClassNode;

public interface Identifier {

    void identify(Context ctx, ClassNode node);
}
