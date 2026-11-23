package vsu.cs.repository.impl.jdbc;

import vsu.cs.db.transaction.JdbcUtils;
import vsu.cs.domain.Car;
import vsu.cs.exception.RepositoryException;
import vsu.cs.repository.CarRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCarRepository implements CarRepository {
    private final DataSource dataSource;

    private static final String FIND_BY_ID = "SELECT id, client_id, brand, model, plate_number, year FROM cars WHERE id = ?";
    private static final String FIND_BY_CLIENT_ID = "SELECT id, client_id, brand, model, plate_number, year FROM cars WHERE client_id = ? ORDER BY id";
    private static final String FIND_ALL = "SELECT id, client_id, brand, model, plate_number, year FROM cars ORDER BY id";
    private static final String DELETE_BY_ID = "DELETE FROM cars WHERE id = ?";
    private static final String EXISTS_BY_ID = "SELECT 1 FROM cars WHERE id = ?";
    private static final String INSERT = "INSERT INTO cars (client_id, brand, model, plate_number, year) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE cars SET client_id = ?, brand = ?, model = ?, plate_number = ?, year = ? WHERE id = ?";


    public JdbcCarRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Car save(Car car) {
        if (car.getId() == null) {
            return this.insert(car);
        }
        return this.update(car);
    }

    @Override
    public Optional<Car> findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(this.mapToCar(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске машины по id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Car> findByClientId(Long clientId) {
        List<Car> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CLIENT_ID)) {
            statement.setLong(1, clientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(this.mapToCar(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске машин клиента: " + clientId, e);
        }
        return result;
    }

    @Override
    public List<Car> findAll() {
        List<Car> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL)) {
            while (resultSet.next()) {
                result.add(this.mapToCar(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении списка машин", e);
        }
        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_ID)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean existsById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при проверке существования машины: " + id, e);
        }
    }

    private Car insert(Car car) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, car.getClientId());
                stmt.setString(2, car.getBrand());
                stmt.setString(3, car.getModel());
                stmt.setString(4, car.getPlateNumber());
                stmt.setInt(5, car.getYear());
                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        car.setId(resultSet.getLong(1));
                    }
                }
                return car;
            }
        });
    }

    private Car update(Car car) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
                stmt.setLong(1, car.getClientId());
                stmt.setString(2, car.getBrand());
                stmt.setString(3, car.getModel());
                stmt.setString(4, car.getPlateNumber());
                stmt.setInt(5, car.getYear());
                stmt.setLong(6, car.getId());
                stmt.executeUpdate();
                return car;
            }
        });
    }

    private Car mapToCar(ResultSet resultSet) throws SQLException {
        return new Car(
                resultSet.getLong("id"),
                resultSet.getLong("client_id"),
                resultSet.getString("brand"),
                resultSet.getString("model"),
                resultSet.getString("plate_number"),
                resultSet.getInt("year")
        );
    }

}
