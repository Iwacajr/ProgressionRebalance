package com.iwaca.progressionrebalance.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Reads typed, range-checked values out of a {@link Properties} file while recording every option
 * (with its documentation) so the same definitions can be rendered back into a commented file.
 *
 * <p>Invalid values never abort loading: they are reported through {@link #problems()} and replaced by
 * the option's default, so a typo in the config cannot crash a server.
 */
public final class ConfigReader {
    private final Properties properties;
    private final List<String> lines = new ArrayList<>();
    private final List<String> problems = new ArrayList<>();
    private boolean missingKeys;

    public ConfigReader(Properties properties) {
        this.properties = properties;
    }

    public void section(String name, String... comment) {
        if (!lines.isEmpty()) {
            lines.add("");
        }
        lines.add("# ==== " + name + " ====");
        for (String line : comment) {
            lines.add("# " + line);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue, String... comment) {
        String raw = raw(key);
        boolean value = defaultValue;
        if (raw != null) {
            if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
                value = Boolean.parseBoolean(raw);
            } else {
                problems.add(key + ": expected true or false but found '" + raw + "', using " + defaultValue);
            }
        }
        write(key, Boolean.toString(value), comment, "Default: " + defaultValue);
        return value;
    }

    public int getInt(String key, int defaultValue, int min, int max, String... comment) {
        String raw = raw(key);
        int value = defaultValue;
        if (raw != null) {
            try {
                value = checkRange(key, Integer.parseInt(raw), min, max, defaultValue);
            } catch (NumberFormatException e) {
                problems.add(key + ": '" + raw + "' is not a whole number, using " + defaultValue);
            }
        }
        write(key, Integer.toString(value), comment, "Default: " + defaultValue + " (range " + min + " to " + max + ")");
        return value;
    }

    public double getDouble(String key, double defaultValue, double min, double max, String... comment) {
        String raw = raw(key);
        double value = defaultValue;
        if (raw != null) {
            try {
                double parsed = Double.parseDouble(raw);
                if (Double.isFinite(parsed)) {
                    value = checkRange(key, parsed, min, max, defaultValue);
                } else {
                    problems.add(key + ": '" + raw + "' is not a finite number, using " + defaultValue);
                }
            } catch (NumberFormatException e) {
                problems.add(key + ": '" + raw + "' is not a number, using " + defaultValue);
            }
        }
        write(key, format(value), comment, "Default: " + format(defaultValue) + " (range " + format(min) + " to " + format(max) + ")");
        return value;
    }

    /** Comma separated list; blank entries are ignored. */
    public List<String> getList(String key, List<String> defaultValue, String... comment) {
        String raw = raw(key);
        List<String> value = raw == null ? defaultValue : Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .toList();
        write(key, String.join(",", value), comment, "Default: " + String.join(",", defaultValue));
        return value;
    }

    public List<String> problems() {
        return List.copyOf(problems);
    }

    /** Whether at least one option was absent from the file, meaning the file should be rewritten. */
    public boolean hadMissingKeys() {
        return missingKeys;
    }

    public String render(String... header) {
        StringBuilder builder = new StringBuilder();
        for (String line : header) {
            builder.append("# ").append(line).append('\n');
        }
        builder.append('\n');
        for (String line : lines) {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    private String raw(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            missingKeys = true;
            return null;
        }
        return value.trim();
    }

    private <T extends Comparable<T>> T checkRange(String key, T value, T min, T max, T defaultValue) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            problems.add(key + ": " + value + " is outside " + min + " to " + max + ", using " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    private void write(String key, String value, String[] comment, String defaultNote) {
        lines.add("");
        for (String line : comment) {
            lines.add("# " + line);
        }
        lines.add("# " + defaultNote);
        lines.add(key + "=" + value);
    }

    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1.0e9) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        return Double.toString(value);
    }
}
