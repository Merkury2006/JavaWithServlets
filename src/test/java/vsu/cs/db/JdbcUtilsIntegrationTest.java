package vsu.cs.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.db.transaction.JdbcUtils;
import vsu.cs.db.transaction.SqlAction;
import vsu.cs.exception.RepositoryException;
import vsu.cs.repository.impl.jdbcImpl.BaseJdbcIntegrationTest;
import vsu.cs.repository.impl.jdbcImpl.JdbcClientRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcUtilsIntegrationTest extends BaseJdbcIntegrationTest {
    private JdbcClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        super.cleanDatabase();
        this.clientRepository = new JdbcClientRepository(dataSource);
    }

    @Test
    void transactionCommitsOnSuccess() {
        JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO clients (full_name) VALUES (?)")) {
                statement.setString(1, "Иван");
                statement.executeUpdate();
            }
        });

        assertEquals(1, clientRepository.findAll().size());
    }

    @Test
    void transactionRollsBackOnSqlException_andWrapsInRepositoryException() {
        RepositoryException exception = assertThrows(RepositoryException.class, () -> {
            JdbcUtils.runInTransaction(dataSource, (SqlAction) connection -> {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO non_existent_table (col) VALUES (?)")) {
                    statement.setString(1, "test");
                    statement.executeUpdate();
                }
            });
        });

        assertInstanceOf(SQLException.class, exception.getCause());

        assertTrue(clientRepository.findAll().isEmpty());
    }

    @Test
    void transactionRollsBackOnException() {
        assertThrows(IllegalStateException.class, () -> {
            JdbcUtils.runInTransaction(dataSource, (SqlAction) connection -> {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO clients (full_name) VALUES (?)")) {
                    statement.setString(1, "Пётр");
                    statement.executeUpdate();
                }
                throw new IllegalStateException("Тестовая ошибка");
            });
        });

        assertTrue(clientRepository.findAll().isEmpty());
    }
}
