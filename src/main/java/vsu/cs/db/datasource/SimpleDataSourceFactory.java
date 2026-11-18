package vsu.cs.db.datasource;

import org.postgresql.ds.PGSimpleDataSource;
import vsu.cs.config.DatabaseConfig;

import javax.sql.DataSource;
import java.util.Objects;

public final class SimpleDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource create(DatabaseConfig config) {
        Objects.requireNonNull(config, "Конфиг базы данных не может быть null");

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(config.url());
        dataSource.setUser(config.user());
        dataSource.setPassword(config.password());
        return dataSource;
    }
}
