package vsu.cs.db;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import vsu.cs.config.DatabaseConfig;
import vsu.cs.db.datasource.HikariDataSourceFactory;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class HikariDataSourceFactoryTest {
    @Test
    void createsHikariDataSource() {
        DatabaseConfig config = new DatabaseConfig("jdbc:postgresql://localhost:5432/test", "user", "pass", 15);

        DataSource ds = new HikariDataSourceFactory().create(config);

        HikariDataSource hikariDs = assertInstanceOf(HikariDataSource.class, ds);

        assertEquals(15, hikariDs.getMaximumPoolSize());
        assertEquals("jdbc:postgresql://localhost:5432/test", hikariDs.getJdbcUrl());
        assertEquals("user", hikariDs.getUsername());

        hikariDs.close();
    }
}
