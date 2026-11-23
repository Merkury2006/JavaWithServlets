package vsu.cs.db;

import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import vsu.cs.config.DatabaseConfig;
import vsu.cs.db.datasource.SimpleDataSourceFactory;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SimpleDataSourceFactoryTest {
    @Test
    void createsPgSimpleDataSource() {
        DatabaseConfig config = new DatabaseConfig("jdbc:postgresql://localhost:5432/test", "user", "pass", 10);

        DataSource ds = new SimpleDataSourceFactory().create(config);

        PGSimpleDataSource pgDs = assertInstanceOf(PGSimpleDataSource.class, ds);

        assertTrue(pgDs.getUrl().startsWith("jdbc:postgresql://localhost:5432/test"));
        assertEquals("user", pgDs.getUser());
    }
}
