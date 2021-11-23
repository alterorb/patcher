package net.alterorb.patcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import okio.HashingSink;
import okio.Okio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LauncherConfigGen {

    private static final Logger LOGGER = LoggerFactory.getLogger(LauncherConfigGen.class);

    private final String server;
    private final String launcherVersion;
    private final Path jarPaths;

    public LauncherConfigGen(String server, String launcherVersion, Path jarPaths) {
        this.server = server;
        this.launcherVersion = launcherVersion;
        this.jarPaths = jarPaths;
    }

    public void generate() throws IOException {
        List<GameConfig> gameConfigs = new ArrayList<>();

        for (FunOrbGame game : FunOrbGame.values()) {
            var jarPath = jarPaths.resolve(game.internalName() + ".jar");
            LOGGER.info("Looking for jar {} at {}", game.internalName(), jarPath);

            if (Files.exists(jarPath)) {
                gameConfigs.add(new GameConfig(
                        game.fancyName(),
                        game.internalName(),
                        game.mainClass(),
                        calculateSha256(jarPath),
                        calculateCrc32(jarPath)
                ));
            } else {
                LOGGER.warn("Could not find jar {} at the directory, no config will be generated for the game.", game.internalName());
            }
        }

        if (gameConfigs.isEmpty()) {
            LOGGER.warn("No game configs were generated, skipping launcher config generation");
            return;
        }
        var launcherConfig = new LauncherConfig(launcherVersion, server, gameConfigs);

        var mapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
        var resolve = jarPaths.resolve("config.json");

        try (var writer = Files.newBufferedWriter(resolve)) {
            mapper.writeValue(writer, launcherConfig);
        }
        LOGGER.info("Generated launcher config with {} games", launcherConfig.games().size());
    }

    private int calculateCrc32(Path jarPath) throws IOException {
        try (var source = Okio.buffer(Okio.source(jarPath));
                var sink = Crc32Sink.of(Okio.blackhole())) {
            source.readAll(sink);
            return sink.crc32();
        }
    }

    private String calculateSha256(Path jarPath) throws IOException {
        try (var source = Okio.buffer(Okio.source(jarPath));
                var sink = HashingSink.sha256(Okio.blackhole())) {
            source.readAll(sink);
            return sink.hash().hex();
        }
    }

    public static record GameConfig(
            String name,
            String internalName,
            String mainClass,
            String gamepackHash,
            int gamecrc
    ) {

    }

    public static record LauncherConfig(
            String version,
            String server,
            List<GameConfig> games
    ) {

    }
}
