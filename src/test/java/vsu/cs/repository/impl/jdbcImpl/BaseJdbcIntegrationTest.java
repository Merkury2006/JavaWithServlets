package vsu.cs.repository.impl.jdbcImpl;

import org.junit.jupiter.api.BeforeAll;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vsu.cs.db.migration.DatabaseMigrator;
import vsu.cs.db.transaction.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Statement;

@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseJdbcIntegrationTest {
    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    protected static DataSource dataSource;

    @BeforeAll
    static void setUpDatabase() {
        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setUrl(POSTGRES.getJdbcUrl());
        source.setUser(POSTGRES.getUsername());
        source.setPassword(POSTGRES.getPassword());
        dataSource = source;

        new DatabaseMigrator(dataSource, "db/changelog/db.changelog-master.yaml").migrate();
    }

    protected void cleanDatabase() {
        JdbcUtils.runInTransaction(dataSource, connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM work_order_items");
                statement.executeUpdate("DELETE FROM work_orders");
                statement.executeUpdate("DELETE FROM cars");
                statement.executeUpdate("DELETE FROM clients");
            }
        });
    }
}
