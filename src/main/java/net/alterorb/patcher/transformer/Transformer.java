package net.alterorb.patcher.transformer;

import net.alterorb.patcher.FunOrbGame;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface Transformer {

    void transform(FunOrbGame game, List<ClassNode> classNodes);
}
