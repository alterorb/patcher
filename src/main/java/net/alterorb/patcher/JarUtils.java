package net.alterorb.patcher;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class JarUtils {

    private JarUtils() {
    }

    public static void saveJar(Path target, List<ClassNode> classNodes) throws IOException {

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

    public static List<ClassNode> loadJar(Path pathToJar) throws IOException {
        return loadJar(pathToJar, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    public static List<ClassNode> loadJar(Path pathToJar, int parsingOptions) throws IOException {
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
