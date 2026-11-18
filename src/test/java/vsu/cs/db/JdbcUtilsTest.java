package vsu.cs.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.db.transaction.JdbcUtils;
import vsu.cs.db.transaction.SqlAction;
import vsu.cs.db.transaction.SqlFunction;
import vsu.cs.exception.RepositoryException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class JdbcUtilsTest {
    private DataSource dataSource;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void commitsOnSuccess() throws SQLException {
        String result = JdbcUtils.runInTransaction(dataSource, conn -> "ok");

        assertEquals("ok", result);
        verify(connection).setAutoCommit(false);
        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void rollsBackOnSqlException() throws SQLException {
        SQLException failure = new SQLException("boom");

        RepositoryException exception = assertThrows(RepositoryException.class, () ->
                JdbcUtils.runInTransaction(dataSource, new SqlFunction<Object>() {
                    @Override
                    public Object apply(Connection conn) throws SQLException {
                        throw failure;
                    }
                })
        );

        assertSame(failure, exception.getCause());
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void rollsBackOnRuntimeException() throws SQLException {
        RuntimeException failure = new IllegalStateException("boom");

        assertThrows(IllegalStateException.class, () -> {
                    JdbcUtils.runInTransaction(dataSource, new SqlFunction<Object>() {
                        @Override
                        public Object apply(Connection connection) throws SQLException {
                            throw  failure;
                        }
                    });
                }
        );

        verify(connection).rollback();
        verify(connection, never()).commit();
    }

    @Test
    void wrapsConnectionFailure() throws SQLException {
        SQLException failure = new SQLException("no connection");
        when(dataSource.getConnection()).thenThrow(failure);

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> JdbcUtils.runInTransaction(dataSource, conn -> "ok"));

        assertSame(failure, exception.getCause());
    }

    @Test
    void voidOverloadWorks() throws SQLException {
        JdbcUtils.runInTransaction(dataSource, new SqlAction() {
            @Override
            public void apply(Connection connection) throws SQLException {
                connection.createStatement();
            }
        });

        verify(connection).commit();
    }
}