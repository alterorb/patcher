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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

public class Patcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Patcher.class);

    private final List<Transformer> transformers;
    private final Path srcDir;
    private final Path outDir;

    private Patcher(List<Transformer> transformers, Path srcDir, Path targetDir) {
        this.transformers = transformers;
        this.srcDir = srcDir;
        this.outDir = targetDir;
    }

    public static Patcher create(Path sourceDir, Path targetDir, Path pubKeyPath) {
        var keyFactory = RSAKeyFactory.create();
        RSAPublicKeySpec oldKeySpec = loadOriginalPubKey(keyFactory);
        RSAPublicKeySpec newKeySpec = loadOrGenerateNewPubKey(keyFactory, pubKeyPath, targetDir);

        var transformers = List.of(
                new ZStringDecrypter(),
                new ZStringArrayInliner(),
                new CheckhostTransformer(),
                new Jdk9MouseFixer(),
                new RSAPubKeyReplacer(oldKeySpec, newKeySpec)
        );
        return new Patcher(transformers, sourceDir, targetDir);
    }

    private static RSAPublicKeySpec loadOrGenerateNewPubKey(RSAKeyFactory keyFactory, Path pubKeyPath, Path targetDir) {
        try {
            if (pubKeyPath == null) {
                return generateKeyPair(keyFactory, targetDir.resolve("rsakey"));
            } else {
                if (!Files.exists(pubKeyPath)) {
                    throw new IllegalArgumentException("Public key file doesn't exist");
                }
                return keyFactory.publicKeySpecFrom(Files.newInputStream(pubKeyPath));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load or generate a new rsa public key", e);
        }
    }

    private static RSAPublicKeySpec generateKeyPair(RSAKeyFactory keyManager, Path outDir) throws IOException {
        LOGGER.info("Generating a new RSA KeyPair...");
        var keyPair = keyManager.generateKeyPair();
        Files.createDirectories(outDir);

        try (var out = Files.newOutputStream(outDir.resolve("rsa-private.key"))) {
            out.write(keyPair.getPrivate().getEncoded());
        }
        try (var out = Files.newOutputStream(outDir.resolve("rsa-public.key"))) {
            out.write(keyPair.getPublic().getEncoded());
        }
        return keyManager.publicKeySpecFrom(keyPair);
    }

    private static RSAPublicKeySpec loadOriginalPubKey(RSAKeyFactory keyFactory) {
        var inputStream = Bootstrap.class.getResourceAsStream("/original-rsa-pubkey.key");

        if (inputStream == null) {
            throw new IllegalStateException("Original rsa pubkey is missing!");
        }
        return keyFactory.publicKeySpecFrom(inputStream);
    }

    public void process() throws IOException {
        for (FunOrbGame game : FunOrbGame.values()) {
            var jarPath = srcDir.resolve(game.internalName() + ".jar");
            LOGGER.debug("Looking for jar {} at {}", game.internalName(), jarPath);

            if (Files.exists(jarPath)) {
                var targetJarPath = outDir.resolve(game.internalName() + ".jar");
                patch(jarPath, targetJarPath);
            } else {
                LOGGER.warn("Could not find jar {} at the source directory.", game.internalName());
            }
        }
    }

    private void patch(Path source, Path target) throws IOException {
        LOGGER.info("Patching {}", target.getFileName());
        var classNodes = JarUtils.loadJar(source);
        for (Transformer transformer : transformers) {
            transformer.transform(classNodes);
        }
        JarUtils.saveJar(target, classNodes);
    }
}
