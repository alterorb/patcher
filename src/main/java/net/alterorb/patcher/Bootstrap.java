package net.alterorb.patcher;

import joptsimple.OptionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws Exception {
        var options = parseOptions(args);

        if (!Files.exists(options.srcDir())) {
            throw new IllegalArgumentException("Source directory doesn't exist");
        }
        Files.createDirectories(options.outDir());

        KeyFactory factory = KeyFactory.getInstance("RSA");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        var publicKeyPath = options.publicKeyPath();
        RSAPublicKeySpec newKeySpec;

        if (publicKeyPath == null) {
            LOGGER.info("Generating a new RSA KeyPair...");
            var keyPair = keyGen.generateKeyPair();
            var keyOutDir = options.outDir().resolve("rsakey");
            Files.createDirectories(keyOutDir);

            try (var out = Files.newOutputStream(keyOutDir.resolve("rsa-private.key"))) {
                out.write(keyPair.getPrivate().getEncoded());
            }
            try (var out = Files.newOutputStream(keyOutDir.resolve("rsa-public.key"))) {
                out.write(keyPair.getPublic().getEncoded());
            }
            newKeySpec = factory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
        } else {
            if (!Files.exists(publicKeyPath)) {
                throw new IllegalArgumentException("Public key file doesn't exist");
            }
            newKeySpec = loadPublicKeySpec(factory, Files.newInputStream(publicKeyPath));
        }
        var inputStream = Bootstrap.class.getResourceAsStream("/original-rsa-pubkey.key");

        if (inputStream == null) {
            throw new IllegalStateException("Original rsa pubkey is missing!");
        }
        var oldKeySpec = loadPublicKeySpec(factory, inputStream);

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

    private static RSAPublicKeySpec loadPublicKeySpec(KeyFactory factory, InputStream inputStream) throws IOException, InvalidKeySpecException {
        var bytes = inputStream.readAllBytes();
        var encodedKeySpec = new X509EncodedKeySpec(bytes);

        var publicKey = factory.generatePublic(encodedKeySpec);

        return factory.getKeySpec(publicKey, RSAPublicKeySpec.class);
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
