package net.alterorb.patcher;

import joptsimple.OptionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class Bootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws Exception {
        var options = parseOptions(args);

        if (!Files.exists(options.srcDir())) {
            throw new IllegalArgumentException("Source directory doesn't exist");
        }
        Files.createDirectories(options.outDir());

        try {
            var patcher = Patcher.create(options.srcDir(), options.outDir(), options.pubKeyPath());
            patcher.process();
        } catch (Exception e) {
            LOGGER.error("Failed to run patcher", e);
        }
    }

    private static Options parseOptions(String[] args) {
        var parser = new OptionParser();
        var pathConverter = new PathValueConverter();

        var patchArg = parser.accepts("patch");
        var gencfgArg = parser.accepts("gencfg");

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
                               .describedAs("The pubkey that will be used as replacement, if none is provided, a new keypair will be generated.");

        var options = parser.parse(args);

        return new Options(
                options.valueOf(srcDirArg),
                options.valueOf(outDirArg),
                options.valueOf(keypairArg)
        );
    }

    public record Options(
            Path srcDir,
            Path outDir,
            String pubKeyPath
    ) {

    }
}
