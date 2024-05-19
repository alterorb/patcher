package net.alterorb.patcher.patcher;

import net.alterorb.patcher.Bootstrap;
import net.alterorb.patcher.FunOrbGame;
import net.alterorb.patcher.indentifier.DependsOn;
import net.alterorb.patcher.indentifier.Identifier;
import net.alterorb.patcher.indentifier.PacketIdentifier;
import net.alterorb.patcher.indentifier.RsaPacketIdentifier;
import net.alterorb.patcher.transformer.CacheRedirector;
import net.alterorb.patcher.transformer.CheckhostTransformer;
import net.alterorb.patcher.transformer.Jdk9MouseFixer;
import net.alterorb.patcher.transformer.RSAPubKeyReplacer;
import net.alterorb.patcher.transformer.Transformer;
import net.alterorb.patcher.transformer.ZStringArrayInliner;
import net.alterorb.patcher.transformer.ZStringDecrypter;
import net.alterorb.patcher.transformer.dungeonassault.SpriteGlowEffectTransformer;
import net.alterorb.patcher.util.RSAKeyFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Patcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Patcher.class);

    private final List<Transformer> transformers;
    private final DirectedMultigraph<Identifier, DefaultEdge> identifiersGraph;
    private final Path srcDir;
    private final Path outDir;

    private Patcher(List<Transformer> transformers, DirectedMultigraph<Identifier, DefaultEdge> identifiersGraph, Path srcDir, Path targetDir) {
        this.transformers = transformers;
        this.identifiersGraph = identifiersGraph;
        this.srcDir = srcDir;
        this.outDir = targetDir;
    }

    public static Patcher create(Path sourceDir, Path targetDir, String pubKey) {
        var keyFactory = RSAKeyFactory.create();
        RSAPublicKeySpec oldKeySpec = loadOriginalPubKey(keyFactory);
        RSAPublicKeySpec newKeySpec = loadOrGenerateNewPubKey(keyFactory, pubKey, targetDir);

        var transformers = List.of(
                new ZStringDecrypter(),
                new ZStringArrayInliner(),
                new CheckhostTransformer(),
                new Jdk9MouseFixer(),
                new RSAPubKeyReplacer(oldKeySpec, newKeySpec),
                new CacheRedirector(),
                new SpriteGlowEffectTransformer()
        );
        var identifiers = List.of(
                new RsaPacketIdentifier(),
                new PacketIdentifier()
        );
        var identifiersGraph = buildIdentifiersGraph(identifiers);
        return new Patcher(transformers, identifiersGraph, sourceDir, targetDir);
    }

    private static RSAPublicKeySpec loadOrGenerateNewPubKey(RSAKeyFactory keyFactory, String pubKey, Path targetDir) {
        try {
            if (pubKey == null) {
                return generateKeyPair(keyFactory, targetDir.resolve("rsakey"));
            } else {
                if (pubKey.startsWith("http://") || pubKey.startsWith("https://")) {
                    LOGGER.info("Loading pubkey from {}", pubKey);
                    return loadPubKeyFromUrl(keyFactory, URI.create(pubKey));
                } else {
                    return loadPubKeyFromFile(keyFactory, Paths.get(pubKey));
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to load or generate a new rsa public key", e);
        }
    }

    private static RSAPublicKeySpec loadPubKeyFromUrl(RSAKeyFactory keyFactory, URI uri) throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(uri)
                                     .build();
            var response = client.send(request, BodyHandlers.ofInputStream());

            try (var inputStream = response.body()) {
                return keyFactory.publicKeySpecFrom(inputStream);
            }
        }
    }

    private static RSAPublicKeySpec loadPubKeyFromFile(RSAKeyFactory keyFactory, Path path) throws IOException {

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Public key file doesn't exist");
        }
        return keyFactory.publicKeySpecFrom(Files.newInputStream(path));
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

    private static DirectedMultigraph<Identifier, DefaultEdge> buildIdentifiersGraph(List<Identifier> identifiers) {
        var dependencyGraph = new DirectedMultigraph<Identifier, DefaultEdge>(DefaultEdge.class);
        var identifierMap = new HashMap<Class<? extends Identifier>, Identifier>();

        for (Identifier identifier : identifiers) {
            dependencyGraph.addVertex(identifier);
            identifierMap.put(identifier.getClass(), identifier);
        }

        for (Identifier identifier : identifiers) {
            var dependsOn = identifier.getClass().getAnnotation(DependsOn.class);

            if (dependsOn == null) {
                continue;
            }
            var dependeeClass = dependsOn.value();
            var dependantOn = identifierMap.get(dependeeClass);

            if (dependantOn == null) {
                throw new IllegalStateException("Class '" + identifier.getClass().getSimpleName() + "' depends on '" + dependsOn.value().getSimpleName() + "' but it is not registered to the graph");
            }
            dependencyGraph.addEdge(dependantOn, identifier);
        }
        return dependencyGraph;
    }

    public void process() throws IOException {
        for (FunOrbGame game : FunOrbGame.values()) {
            var jarPath = srcDir.resolve(game.internalName() + ".jar");
            LOGGER.debug("Looking for jar {} at {}", game.internalName(), jarPath);

            if (Files.exists(jarPath)) {
                var targetJarPath = outDir.resolve(game.internalName() + ".jar");
                var context = new Context(game);

                patch(context, jarPath, targetJarPath);
            } else {
                LOGGER.warn("Could not find jar {} at the source directory.", game.internalName());
            }
        }
    }

    private void patch(Context context, Path source, Path target) throws IOException {
        LOGGER.info("Patching {}", target.getFileName());
        var classNodes = loadJar(source);
        var iterator = new TopologicalOrderIterator<>(identifiersGraph);

        iterator.forEachRemaining(identifier -> {
            LOGGER.trace("Running identifier {}", identifier.getClass().getSimpleName());
            for (var classNode : classNodes) {
                identifier.identify(context, classNode);
            }
        });

        for (var transformer : transformers) {
            transformer.transform(context, classNodes);
        }
        saveJar(target, classNodes);
    }

    private void saveJar(Path target, List<ClassNode> classNodes) throws IOException {

        try (var output = new JarOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            for (var node : classNodes) {
                var entry = new JarEntry(node.name + ".class");
                output.putNextEntry(entry);

                var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                output.write(writer.toByteArray());

                output.closeEntry();
            }
        }
    }

    private List<ClassNode> loadJar(Path pathToJar) throws IOException {
        return loadJar(pathToJar, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    private List<ClassNode> loadJar(Path pathToJar, int parsingOptions) throws IOException {
        List<ClassNode> classNodes = new ArrayList<>();

        try (var jarFile = new JarFile(pathToJar.toString())) {
            var enums = jarFile.entries();

            while (enums.hasMoreElements()) {
                var entry = (JarEntry) enums.nextElement();

                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                var classReader = new ClassReader(jarFile.getInputStream(entry));
                var classNode = new ClassNode();

                classReader.accept(classNode, parsingOptions);
                classNodes.add(classNode);
            }
        }
        return classNodes;
    }
}