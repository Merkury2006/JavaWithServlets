package vsu.cs.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseConfigTest {

    @Test
    void rejectsNullUrl() {
        assertThrows(NullPointerException.class,
                () -> new DatabaseConfig(null, "user", "password", 10));
    }

    @Test
    void rejectsNullUser() {
        assertThrows(NullPointerException.class,
                () -> new DatabaseConfig("url", null, "password", 10));
    }

    @Test
    void rejectsNullPassword() {
        assertThrows(NullPointerException.class,
                () -> new DatabaseConfig("url", "user", null, 10));
    }

    @Test
    void rejectsNonPositivePoolSize() {
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConfig("url", "user", "password", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConfig("url", "user", "password", -5));
    }
}