package vsu.cs.config;

import java.util.Objects;

public record DatabaseConfig(String url, String user, String password, int maxPoolSize) {
    public DatabaseConfig {
        Objects.requireNonNull(url, "URL базы данных не может быть null");
        Objects.requireNonNull(user, "Пользователь базы данных не может быть null");
        Objects.requireNonNull(password, "Пароль базы данных не может быть null");
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize должен быть > 0");
        }
    }
}
