package net.alterorb.patcher;

import net.alterorb.patcher.transformer.CheckhostTransformer;
import net.alterorb.patcher.transformer.Jdk9MouseFixer;
import net.alterorb.patcher.transformer.RSAPubKeyReplacer;
import net.alterorb.patcher.transformer.Transformer;
import net.alterorb.patcher.transformer.ZStringArrayInliner;
import net.alterorb.patcher.transformer.ZStringDecrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

public class Patcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Patcher.class);

    private final List<Transformer> transformers;

    private Patcher(List<Transformer> transformers) {
        this.transformers = transformers;
    }

    public static Patcher create(RSAPublicKeySpec oldKeySpec, RSAPublicKeySpec newKeySpec) {
        var transformers = List.of(
                new ZStringDecrypter(),
                new ZStringArrayInliner(),
                new CheckhostTransformer(),
                new Jdk9MouseFixer(),
                new RSAPubKeyReplacer(oldKeySpec, newKeySpec)
        );
        return new Patcher(transformers);
    }

    public void patch(Path source, Path target) throws IOException {
        LOGGER.info("Patching {}", target.getFileName());
        var classNodes = JarUtils.loadJar(source);
        for (Transformer transformer : transformers) {
            transformer.transform(classNodes);
        }
        JarUtils.saveJar(target, classNodes);
    }
}
