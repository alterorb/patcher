package net.alterorb.patcher;

import joptsimple.OptionParser;
import joptsimple.ValueConverter;
import net.alterorb.patcher.config.LauncherConfigGen;
import net.alterorb.patcher.patcher.Patcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        var options = parseOptions(args);

        switch (options) {
            case PatcherOptions patcherOptions -> launchPatcher(patcherOptions);
            case ConfigGenOptions configGenOptions -> launchConfigGen(configGenOptions);
            case null, default -> LOGGER.error("Unsupported/unknown task");
        }
    }

    private static void launchPatcher(PatcherOptions options) {
        if (!Files.exists(options.srcDir())) {
            throw new IllegalArgumentException("Source directory doesn't exist");
        }

        try {
            Files.createDirectories(options.outDir());
            var patcher = Patcher.create(options.srcDir(), options.outDir(), options.pubKeyPath());
            patcher.process();
        } catch (Exception e) {
            LOGGER.error("Failed to run patcher", e);
        }
    }

    private static void launchConfigGen(ConfigGenOptions options) {
        if (!Files.exists(options.srcDir())) {
            throw new IllegalArgumentException("Source directory doesn't exist");
        }

        try {
            var configGen = LauncherConfigGen.create(options.server(), options.version(), options.srcDir());
            configGen.generate();
        } catch (Exception e) {
            LOGGER.error("Failed to run config gen", e);
        }
    }

    private static Options parseOptions(String[] args) {
        var parser = new OptionParser();
        var pathConverter = new PathValueConverter();

        var patchArg = parser.accepts("patch");
        var gencfgArg = parser.accepts("gencfg");

        parser.mutuallyExclusive(patchArg, gencfgArg);

        var srcDirArg = parser.accepts("src")
                              .withRequiredArg()
                              .describedAs("The source directory")
                              .required()
                              .withValuesConvertedBy(pathConverter);
        var outDirArg = parser.accepts("out")
                              .requiredIf(patchArg)
                              .withRequiredArg()
                              .describedAs("The output directory")
                              .withValuesConvertedBy(pathConverter);
        var keypairArg = parser.accepts("pubkey")
                               .availableIf(patchArg)
                               .withRequiredArg()
                               .describedAs("The pubkey that will be used as replacement, if none is provided, a new keypair will be generated.");

        var serverArg = parser.accepts("server")
                              .availableIf(gencfgArg)
                              .withRequiredArg()
                              .describedAs("The server url to be injected into the generated config file.");
        var versionArg = parser.accepts("version")
                               .availableIf(gencfgArg)
                               .withRequiredArg()
                               .describedAs("The launcher version to be injected into the generated config file.");

        var options = parser.parse(args);

        if (options.has(patchArg)) {

            return new PatcherOptions(
                    options.valueOf(srcDirArg),
                    options.valueOf(outDirArg),
                    options.valueOf(keypairArg)
            );
        } else if (options.has(gencfgArg)) {
            return new ConfigGenOptions(
                    options.valueOf(srcDirArg),
                    options.valueOf(serverArg),
                    options.valueOf(versionArg)
            );
        } else {
            return null;
        }
    }

    private static class PathValueConverter implements ValueConverter<Path> {

        @Override
        public Path convert(String value) {
            return Path.of(value);
        }

        @Override
        public Class<? extends Path> valueType() {
            return Path.class;
        }

        @Override
        public String valuePattern() {
            return null;
        }
    }

    private interface Options {

    }

    private record PatcherOptions(
            Path srcDir,
            Path outDir,
            String pubKeyPath
    ) implements Options {

    }

    private record ConfigGenOptions(
            Path srcDir,
            String server,
            String version
    ) implements Options {

    }
}
