package net.alterorb.patcher;

import joptsimple.OptionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws Exception {
        var options = parseOptions(args);

        if (!Files.exists(options.srcDir())) {
            throw new IllegalArgumentException("Source directory doesn't exist");
        }
        Files.createDirectories(options.outDir());

        var keyManager = RSAKeyFactory.create();

        var publicKeyPath = options.publicKeyPath();
        RSAPublicKeySpec newKeySpec;

        if (publicKeyPath == null) {
            newKeySpec = generateKeyPair(keyManager, options.outDir().resolve("rsakey"));
        } else {
            if (!Files.exists(publicKeyPath)) {
                throw new IllegalArgumentException("Public key file doesn't exist");
            }
            newKeySpec = keyManager.publicKeySpecFrom(Files.newInputStream(publicKeyPath));
        }
        var inputStream = Bootstrap.class.getResourceAsStream("/original-rsa-pubkey.key");

        if (inputStream == null) {
            throw new IllegalStateException("Original rsa pubkey is missing!");
        }
        var oldKeySpec = keyManager.publicKeySpecFrom(inputStream);

        var patcher = Patcher.create(oldKeySpec, newKeySpec);

        var jarPaths = Files.list(options.srcDir())
                            .filter(path -> path.toString().endsWith(".jar"))
                            .toList();
        LOGGER.info("Found {} jars to process", jarPaths.size());

        for (Path jarPath : jarPaths) {
            var jarName = jarPath.getFileName().toString();

            patcher.patch(jarPath, options.outDir().resolve(jarName));
        }
    }

    private static RSAPublicKeySpec generateKeyPair(RSAKeyFactory keyManager, Path outDir) throws IOException, InvalidKeySpecException {
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

    private static Options parseOptions(String[] args) {
        var parser = new OptionParser();
        var pathConverter = new PathValueConverter();
        var srcDirArg = parser.accepts("src")
                              .withRequiredArg()
                              .describedAs("The source directory")
                              .required()
                              .withValuesConvertedBy(pathConverter);
        var outDirArg = parser.accepts("out")
                              .withRequiredArg()
                              .describedAs("The output directory")
                              .required()
                              .withValuesConvertedBy(pathConverter);
        var keypairArg = parser.accepts("pubkey")
                               .withRequiredArg()
                               .describedAs("The pubkey that will be used as replacement, if none is provided, a new keypair will be generated.")
                               .withValuesConvertedBy(pathConverter);

        var options = parser.parse(args);

        return new Options(
                options.valueOf(srcDirArg),
                options.valueOf(outDirArg),
                options.valueOf(keypairArg)
        );
    }

    public static record Options(
            Path srcDir,
            Path outDir,
            Path publicKeyPath
    ) {

    }
}
