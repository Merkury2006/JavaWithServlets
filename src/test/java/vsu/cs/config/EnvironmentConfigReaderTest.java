package vsu.cs.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnvironmentConfigReaderTest {
    private final EnvironmentConfigReader reader = new EnvironmentConfigReader();

    @Test
    void usesDefaultsWhenEnvironmentIsEmpty() {
        DatabaseConfig config = reader.read(Map.of());

        assertEquals("jdbc:postgresql://localhost:5432/autoService", config.url());
        assertEquals("autoService", config.user());
        assertEquals("autoService", config.password());
    }

    @Test
    void readsValuesFromMap() {
        DatabaseConfig config = reader.read(Map.of(
                "DB_URL", "jdbc:postgresql://db:5432/other",
                "DB_USER", "reader",
                "DB_PASSWORD", "secret"
        ));

        assertEquals("jdbc:postgresql://db:5432/other", config.url());
        assertEquals("reader", config.user());
        assertEquals("secret", config.password());
    }

    @Test
    void fallsBackToDefaultsForBlankValues() {
        DatabaseConfig config = reader.read(Map.of(
                "DB_URL", " ",
                "DB_USER", "",
                "DB_PASSWORD", "   "
        ));

        assertEquals("jdbc:postgresql://localhost:5432/autoService", config.url());
        assertEquals("autoService", config.user());
        assertEquals("autoService", config.password());
    }

    @Test
    void partiallyOverridesOnlyProvidedKeys() {
        DatabaseConfig config = reader.read(Map.of("DB_URL", "jdbc:postgresql://only:5432/url"));

        assertEquals("jdbc:postgresql://only:5432/url", config.url());
        assertEquals("autoService", config.user());
        assertEquals("autoService", config.password());
    }

    @Test
    void rejectsNullMap() {
        assertThrows(NullPointerException.class,
                () -> reader.read((Map<String, String>) null));
    }

    @Test
    void readsDotEnvFile(@TempDir Path dir) throws IOException {
        Path dotEnv = dir.resolve(".env");
        Files.writeString(dotEnv, """
                # комментарий
                DB_URL=jdbc:postgresql://file:5432/fromfile
                DB_USER=fromfile
                DB_PASSWORD=fromfile
                """);

        Map<String, String> env = new EnvironmentConfigReader(dotEnv).loadEnvironment();

        assertEquals("jdbc:postgresql://file:5432/fromfile", env.get("DB_URL"));
        assertEquals("fromfile", env.get("DB_USER"));
        assertEquals("fromfile", env.get("DB_PASSWORD"));
    }

    @Test
    void ignoresCommentsAndBlankLines(@TempDir Path dir) throws IOException {
        Path dotEnv = dir.resolve(".env");
        Files.writeString(dotEnv, """
                # это комментарий

                DB_URL=jdbc:postgresql://x:1/y
                # ещё комментарий
                DB_USER=u
                """);

        Map<String, String> env = new EnvironmentConfigReader(dotEnv).loadEnvironment();

        assertEquals("jdbc:postgresql://x:1/y", env.get("DB_URL"));
        assertEquals("u", env.get("DB_USER"));
    }

    @Test
    void ignoresMalformedLines(@TempDir Path dir) throws IOException {
        Path dotEnv = dir.resolve(".env");
        Files.writeString(dotEnv, """
                NO_EQUALS_SIGN
                DB_URL=jdbc:postgresql://x:1/y
                =valueWithoutKey
                """);

        Map<String, String> env = new EnvironmentConfigReader(dotEnv).loadEnvironment();

        assertEquals("jdbc:postgresql://x:1/y", env.get("DB_URL"));
    }

    @Test
    void ignoresMissingDotEnv(@TempDir Path dir) {
        Path missing = dir.resolve(".env");

        Map<String, String> env = new EnvironmentConfigReader(missing).loadEnvironment();

        assertEquals("jdbc:postgresql://localhost:5432/autoService", reader.read(Map.of()).url());
    }

    @Test
    void systemEnvOverridesDotEnv() {
        Map<String, String> fileValues = new HashMap<>();
        fileValues.put("DB_URL", "jdbc:postgresql://fromfile:1/file");
        fileValues.put("DB_USER", "fileuser");

        Map<String, String> systemValues = Map.of("DB_USER", "systemuser");
        Map<String, String> merged = new HashMap<>(fileValues);
        merged.putAll(systemValues);

        assertEquals("jdbc:postgresql://fromfile:1/file", merged.get("DB_URL"));
        assertEquals("systemuser", merged.get("DB_USER"));
    }

    @Test
    void rejectsNullDotEnvPath() {
        assertThrows(NullPointerException.class,
                () -> new EnvironmentConfigReader(null));
    }
}
