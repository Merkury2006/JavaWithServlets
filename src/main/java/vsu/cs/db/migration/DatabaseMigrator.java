package vsu.cs.db.migration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import vsu.cs.exception.RepositoryException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Objects;

public class DatabaseMigrator {
    private final DataSource dataSource;
    private final String changelogPath;

    public DatabaseMigrator(DataSource dataSource, String changelogPath) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource не может быть null");
        this.changelogPath = Objects.requireNonNull(changelogPath, "Путь к changelog не может быть null");
    }

    public void migrate() {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            try (Liquibase liquibase = new Liquibase(changelogPath, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update();
            }
        } catch (Exception e) {
            throw new RepositoryException("Не удалось применить миграции базы данных: " + e.getMessage(), e);
        }
    }
}