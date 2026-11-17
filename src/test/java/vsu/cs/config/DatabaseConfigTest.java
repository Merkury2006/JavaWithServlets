package vsu.cs.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatabaseConfigTest {

    @Test
    void rejectsNullUrl() {
        assertThrows(NullPointerException.class,
                () -> new DatabaseConfig(null, "user", "password"));
    }

    @Test
    void rejectsNullUser() {
        assertThrows(NullPointerException.class,
                () -> new DatabaseConfig("url", null, "password"));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(NullPointerException.class,
                () -> new DatabaseConfig("url", "user", null));
    }
}