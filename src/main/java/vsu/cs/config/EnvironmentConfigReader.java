package vsu.cs.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class EnvironmentConfigReader {
    static final String URL_VARIABLE = "DB_URL";
    static final String USER_VARIABLE = "DB_USER";
    static final String PASSWORD_VARIABLE = "DB_PASSWORD";

    static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/autoService";
    static final String DEFAULT_USER = "autoService";
    static final String DEFAULT_PASSWORD = "autoService";

    private final Path dotEnvPath;

    public EnvironmentConfigReader() {
        this(Path.of(".env"));
    }

    public EnvironmentConfigReader(Path dotEnvPath) {
        this.dotEnvPath = Objects.requireNonNull(dotEnvPath, "Путь к .env не может быть null");
    }

    public DatabaseConfig read() {
        return this.read(this.loadEnvironment());
    }

    Map<String, String> loadEnvironment() {
        Map<String, String> merged = new HashMap<>(readDotEnv(dotEnvPath));
        merged.putAll(System.getenv());
        return merged;
    }

    DatabaseConfig read(Map<String, String> environment) {
        Objects.requireNonNull(environment, "Окружение не может быть null");
        return new DatabaseConfig(
                valueOrDefault(environment, URL_VARIABLE, DEFAULT_URL),
                valueOrDefault(environment, USER_VARIABLE, DEFAULT_USER),
                valueOrDefault(environment, PASSWORD_VARIABLE, DEFAULT_PASSWORD)
        );
    }

    private static Map<String, String> readDotEnv(Path path) {
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                result.put(key, value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать " + path, e);
        }
        return result;
    }

    private static String valueOrDefault(Map<String, String> environment, String name, String defaultValue) {
        String value = environment.get(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}