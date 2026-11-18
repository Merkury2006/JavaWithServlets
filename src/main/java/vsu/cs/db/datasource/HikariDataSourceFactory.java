package vsu.cs.db.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import vsu.cs.config.DatabaseConfig;

import javax.sql.DataSource;
import java.util.Objects;

public final class HikariDataSourceFactory implements DataSourceFactory {
    @Override
    public DataSource create(DatabaseConfig config) {
        Objects.requireNonNull(config, "Конфиг базы данных не может быть null");

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.url());
        hikari.setUsername(config.user());
        hikari.setPassword(config.password());
        hikari.setMaximumPoolSize(config.maxPoolSize());
        hikari.setInitializationFailTimeout(-1);
        return new HikariDataSource(hikari);
    }
}
