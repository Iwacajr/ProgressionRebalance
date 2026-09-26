package com.iwaca.progressionrebalance.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModConfigTest {
    private static ConfigReader readerFor(String contents) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(contents));
        return new ConfigReader(properties);
    }

    @Test
    void defaultsMatchTheDesign() {
        ModConfig config = ModConfig.defaults();
        assertEquals(2.5, config.transport().minecartSpeedMultiplier());
        assertEquals(8, config.rails().copperRailOutput());
        assertEquals(16, config.rails().ironRailOutput());
        assertEquals(12, config.rails().poweredRailOutput());
        assertEquals(128, config.gold().toolDurability());
        assertEquals(11, config.gold().armorDurabilityMultiplier());
        assertEquals(180, config.gold().swordDurability());
        assertEquals(6.5, config.gold().swordAttackDamage());
        assertEquals(1.8, config.gold().swordAttackSpeed());
        assertEquals(8.0, config.gold().axeAttackDamage());
        assertEquals(1.1, config.gold().axeAttackSpeed());
        assertEquals(18, config.gold().helmetArmor() + config.gold().chestplateArmor()
                + config.gold().leggingsArmor() + config.gold().bootsArmor());
        assertEquals(3, config.enchanting().maxPages());
        assertEquals(3, config.potions().maxMixedEffects());
        assertEquals(0.5, config.luck().chestBonusRollChancePerLuck());
        assertTrue(config.villagers().enableTradeRebalance());
        assertTrue(config.loot().enableStructureLootRebalance());
    }

    @Test
    void validValuesAreRead() throws IOException {
        ConfigReader reader = readerFor("""
                transport.minecartSpeedMultiplier=2.0
                rails.poweredRailOutput=10
                enchanting.lapisPages=false
                potions.combatEffects=minecraft:strength, minecraft:regeneration ,
                """);
        ModConfig config = ModConfig.read(reader);
        assertEquals(2.0, config.transport().minecartSpeedMultiplier());
        assertEquals(10, config.rails().poweredRailOutput());
        assertFalse(config.enchanting().lapisPages());
        assertEquals(1, config.enchanting().effectiveMaxPages());
        assertEquals(List.of("minecraft:strength", "minecraft:regeneration"), config.potions().combatEffects());
        assertTrue(reader.problems().isEmpty());
        assertTrue(reader.hadMissingKeys());
    }

    @Test
    void invalidValuesFallBackToDefaultsAndAreReported() throws IOException {
        ConfigReader reader = readerFor("""
                transport.minecartSpeedMultiplier=5.0
                rails.copperRailOutput=lots
                gold.enabled=yes
                luck.maxEffectiveLuck=NaN
                """);
        ModConfig config = ModConfig.read(reader);
        assertEquals(2.5, config.transport().minecartSpeedMultiplier());
        assertEquals(8, config.rails().copperRailOutput());
        assertTrue(config.gold().enabled());
        assertEquals(4.0, config.luck().maxEffectiveLuck());
        assertEquals(4, reader.problems().size());
    }

    @Test
    void renderedFileReadsBackToTheSameConfig() throws IOException {
        ConfigReader first = readerFor("potions.maxMixedEffects=4\nrails.ironRailOutput=24\n");
        ModConfig original = ModConfig.read(first);
        String rendered = first.render("header");

        ConfigReader second = readerFor(rendered);
        assertEquals(original, ModConfig.read(second));
        assertFalse(second.hadMissingKeys());
        assertTrue(second.problems().isEmpty());
        assertTrue(rendered.contains("# Default: 3 (range 2 to 8)"));
    }

    @Test
    void loadWritesADocumentedDefaultFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("config").resolve(ConfigManager.FILE_NAME);
        ModConfig config = ConfigManager.load(file);
        assertEquals(ModConfig.defaults(), config);
        String written = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(written.contains("transport.minecartSpeedMultiplier=2.5"));
        assertTrue(written.contains("# ==== Potions ===="));
    }
}
