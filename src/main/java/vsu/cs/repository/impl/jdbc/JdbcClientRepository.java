package vsu.cs.repository.impl.jdbc;

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

    private static final String FIND_BY_ID = "SELECT id, full_name, phone, email FROM clients WHERE id = ?";
    private static final String FIND_ALL = "SELECT id, full_name, phone, email FROM clients ORDER BY id";
    private static final String DELETE_BY_ID = "DELETE FROM clients WHERE id = ?";
    private static final String EXISTS_BY_ID = "SELECT 1 FROM clients WHERE id = ?";
    private static final String INSERT = "INSERT INTO clients (full_name, phone, email) VALUES (?, ?, ?)";
    private static final String UPDATE = "UPDATE clients SET full_name = ?, phone = ?, email = ? WHERE id = ?";


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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
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
        List<Client> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL)) {
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
            throw new RepositoryException("Ошибка при проверке существования клиента: " + id, e);
        }
    }

    private Client insert(Client client) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
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
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
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
