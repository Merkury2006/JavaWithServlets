package vsu.cs.db.transaction;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlAction {
    void apply(Connection connection) throws SQLException;
}