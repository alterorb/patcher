package net.alterorb.patcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.alterorb.patcher.FunOrbGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.CRC32;

public class LauncherConfigGen {

    private static final Logger LOGGER = LoggerFactory.getLogger(LauncherConfigGen.class);

    private final String server;
    private final String launcherVersion;
    private final Path jarPaths;

    private LauncherConfigGen(String server, String launcherVersion, Path jarPaths) {
        this.server = server;
        this.launcherVersion = launcherVersion;
        this.jarPaths = jarPaths;
    }

    public static LauncherConfigGen create(String server, String launcherVersion, Path jarPaths) {
        return new LauncherConfigGen(server, launcherVersion, jarPaths);
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
        var configPath = jarPaths.resolve("config.json");

        try (var writer = Files.newBufferedWriter(configPath)) {
            mapper.writeValue(writer, launcherConfig);
        }
        LOGGER.info("Generated launcher config with {} games to file {}", launcherConfig.games().size(), configPath);
    }

    private int calculateCrc32(Path jarPath) throws IOException {
        var crc32 = new CRC32();

        try (var stream = Files.newInputStream(jarPath)) {
            var buffer = new byte[4096];
            var read = -1;

            while ((read = stream.read(buffer)) != -1) {
                crc32.update(buffer, 0, read);
            }
            return (int) crc32.getValue();
        }
    }

    private String calculateSha256(Path jarPath) throws IOException {
        try {
            var sha256 = MessageDigest.getInstance("SHA-256");

            try (var stream = Files.newInputStream(jarPath)) {
                var buffer = new byte[4096];
                var read = -1;

                while ((read = stream.read(buffer)) != -1) {
                    sha256.update(buffer, 0, read);
                }
                return HexFormat.of().formatHex(sha256.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public record GameConfig(
            String name,
            String internalName,
            String mainClass,
            String gamepackHash,
            int gamecrc
    ) {

    }

    public record LauncherConfig(
            String version,
            String server,
            List<GameConfig> games
    ) {

    }
}
