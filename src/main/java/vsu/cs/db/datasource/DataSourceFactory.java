package vsu.cs.db.datasource;

import vsu.cs.config.DatabaseConfig;

import javax.sql.DataSource;

public interface DataSourceFactory {
    DataSource create(DatabaseConfig config);
}
