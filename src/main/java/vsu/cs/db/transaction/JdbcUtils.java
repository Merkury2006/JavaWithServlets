package vsu.cs.db.transaction;

import vsu.cs.exception.RepositoryException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class JdbcUtils {

    private JdbcUtils() {
    }

    public static <T> T runInTransaction(DataSource dataSource, SqlFunction<T> action) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = action.apply(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при работе с базой данных: " + e.getMessage(), e);
        }
    }

    public static void runInTransaction(DataSource dataSource, SqlAction action) {
        runInTransaction(dataSource, connection -> {
            action.apply(connection);
            return null;
        });
    }
}