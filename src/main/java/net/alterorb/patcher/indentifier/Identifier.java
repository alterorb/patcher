package net.alterorb.patcher.indentifier;

import net.alterorb.patcher.patcher.IdentifierContext;
import org.objectweb.asm.tree.ClassNode;

public interface Identifier {

    void identify(IdentifierContext ctx, ClassNode node);
}
