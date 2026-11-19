package vsu.cs.repository.impl.jdbcImpl;

import vsu.cs.db.transaction.JdbcUtils;
import vsu.cs.domain.Client;
import vsu.cs.exception.RepositoryException;
import vsu.cs.repository.ClientRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcClientRepository implements ClientRepository {
    private final DataSource dataSource;

    public JdbcClientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Client save(Client client) {
        if (client.getId() == null) {
            return this.insert(client);
        }
        return this.update(client);
    }

    @Override
    public Optional<Client> findById(Long id) {
        String sql = "SELECT id, full_name, phone, email FROM clients WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(this.mapToClient(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске клиента по id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, full_name, phone, email FROM clients ORDER BY id";
        List<Client> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                result.add(this.mapToClient(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении списка клиентов", e);
        }
        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM clients WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при проверке существования клиента: " + id, e);
        }
    }

    private Client insert(Client client) {
        String sql = "INSERT INTO clients (full_name, phone, email) VALUES (?, ?, ?)";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, client.getFullName());
                stmt.setString(2, client.getPhone());
                stmt.setString(3, client.getEmail());
                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        client.setId(resultSet.getLong(1));
                    }
                }
                return client;
            }
        });
    }

    private Client update(Client client) {
        String sql = "UPDATE clients SET full_name = ?, phone = ?, email = ? WHERE id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, client.getFullName());
                stmt.setString(2, client.getPhone());
                stmt.setString(3, client.getEmail());
                stmt.setLong(4, client.getId());
                stmt.executeUpdate();
                return client;
            }
        });
    }

    private Client mapToClient(ResultSet resultSet) throws SQLException {
        return new Client(
                resultSet.getLong("id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email")
        );
    }

}
