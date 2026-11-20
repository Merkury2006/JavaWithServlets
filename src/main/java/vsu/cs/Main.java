package vsu.cs;

import vsu.cs.config.DatabaseConfig;
import vsu.cs.config.EnvironmentConfigReader;
import vsu.cs.db.datasource.DataSourceFactory;
import vsu.cs.db.datasource.SimpleDataSourceFactory;
import vsu.cs.db.migration.DatabaseMigrator;
import vsu.cs.ui.ConsoleApp;

import javax.sql.DataSource;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig config = new EnvironmentConfigReader().read();

        DataSourceFactory factory = new SimpleDataSourceFactory();
        DataSource dataSource = factory.create(config);

        try {
            new DatabaseMigrator(dataSource, "db/changelog/db.changelog-master.yaml").migrate();

            AppContainer container = AppFactory.createJdbc(dataSource);

            new ConsoleApp(container.clientFacade, container.carFacade, container.workOrderFacade).start();
    } finally {
            if (dataSource instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    System.err.println("Не удалось закрыть DataSource: " + e.getMessage());
                }
            }
        }
    }
}