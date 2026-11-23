package vsu.cs.db;

import org.junit.jupiter.api.Test;
import vsu.cs.db.migration.DatabaseMigrator;
import vsu.cs.exception.RepositoryException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class DatabaseMigratorTest {

    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";

    @Test
    void rejectsNullDataSource() {
        assertThrows(NullPointerException.class, () -> new DatabaseMigrator(null, CHANGELOG));
    }

    @Test
    void rejectsNullChangelogPath() {
        assertThrows(NullPointerException.class, () -> new DatabaseMigrator(mock(DataSource.class), null));
    }

    @Test
    void wrapsConnectionFailure() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        SQLException failure = new SQLException("connection refused");
        when(dataSource.getConnection()).thenThrow(failure);

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> new DatabaseMigrator(dataSource, CHANGELOG).migrate());

        assertSame(failure, exception.getCause());
        assertTrue(exception.getMessage().startsWith("Не удалось применить миграции базы данных"));
    }

    @Test
    void wrapsLiquibaseFailureAndClosesConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenThrow(new SQLException("broken metadata"));

        assertThrows(RepositoryException.class, () -> new DatabaseMigrator(dataSource, CHANGELOG).migrate());

        verify(connection).close();
    }
}