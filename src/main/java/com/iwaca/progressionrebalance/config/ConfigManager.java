package com.iwaca.progressionrebalance.config;

import com.iwaca.progressionrebalance.ProgressionRebalance;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Loads {@code config/progressionrebalance.properties} exactly once.
 *
 * <p>Loading is lazy because some values are needed while vanilla registries bootstrap (potion durations),
 * which can happen before the mod initializer runs.
 */
public final class ConfigManager {
    public static final String FILE_NAME = ProgressionRebalance.MOD_ID + ".properties";

    private static volatile ModConfig config;

    private ConfigManager() {
    }

    public static ModConfig get() {
        ModConfig current = config;
        if (current == null) {
            synchronized (ConfigManager.class) {
                current = config;
                if (current == null) {
                    current = load(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME));
                    config = current;
                }
            }
        }
        return current;
    }

    static ModConfig load(Path path) {
        Properties properties = new Properties();
        boolean exists = Files.isRegularFile(path);
        if (exists) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                ProgressionRebalance.LOGGER.error("Could not read {}, using default values", path, e);
                return ModConfig.defaults();
            }
        }

        ConfigReader reader = new ConfigReader(properties);
        ModConfig loaded = ModConfig.read(reader);
        for (String problem : reader.problems()) {
            ProgressionRebalance.LOGGER.warn("Invalid config value in {}: {}", FILE_NAME, problem);
        }

        if (!exists || reader.hadMissingKeys()) {
            write(path, reader);
        }
        return loaded;
    }

    private static void write(Path path, ConfigReader reader) {
        String contents = reader.render(
                "Progression Rebalance configuration.",
                "Changes take effect after restarting the game or server.",
                "Use the same values on the server and on every client: durability, potion durations and",
                "stack sizes are item/registry defaults that each side computes for itself.");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, contents, StandardCharsets.UTF_8);
        } catch (IOException e) {
            ProgressionRebalance.LOGGER.error("Could not write default config to {}", path, e);
        }
    }
}
