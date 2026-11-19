package vsu.cs.repository.impl.jdbcImpl;

import vsu.cs.db.transaction.JdbcUtils;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.RepositoryException;
import vsu.cs.repository.WorkOrderRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkOrderRepository implements WorkOrderRepository {
    private final DataSource dataSource;

    public JdbcWorkOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public WorkOrder save(WorkOrder order) {
        if (order.getId() == null) {
            return this.insert(order);
        }
        return this.update(order);
    }

    @Override
    public List<WorkOrder> findByCarId(Long carId) {
        String sql = "SELECT id, car_id, status, created_at, total_price FROM work_orders WHERE car_id = ? ORDER BY id";
        return this.queryList(sql, carId);
    }

    @Override
    public List<WorkOrder> findByStatus(OrderStatus status) {
        String sql = "SELECT id, car_id, status, created_at, total_price FROM work_orders WHERE status = ? ORDER BY id";
        return this.queryList(sql, status.name());
    }

    @Override
    public Optional<WorkOrder> findById(Long id) {
        String sql = "SELECT id, car_id, status, created_at, total_price FROM work_orders WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(this.mapToWorkOrder(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске заказа по id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<WorkOrder> findAll() {
        String sql = "SELECT id, car_id, status, created_at, total_price FROM work_orders ORDER BY id";
        List<WorkOrder> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                result.add(this.mapToWorkOrder(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении списка заказов", e);
        }
        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM work_orders WHERE id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM work_orders WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при проверке существования заказа: " + id, e);
        }
    }

    private WorkOrder insert(WorkOrder order) {
        String sql = "INSERT INTO work_orders (car_id, status, created_at, total_price) VALUES (?, ?, ?, ?)";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, order.getCarId());
                stmt.setString(2, order.getStatus().name());
                stmt.setTimestamp(3, Timestamp.valueOf(order.getCreatedAt()));
                stmt.setDouble(4, order.getTotalPrice());
                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        order.setId(resultSet.getLong(1));
                    }
                }
                return order;
            }
        });
    }

    private WorkOrder update(WorkOrder order) {
        String sql = "UPDATE work_orders SET car_id = ?, status = ?, total_price = ? WHERE id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, order.getCarId());
                stmt.setString(2, order.getStatus().name());
                stmt.setDouble(3, order.getTotalPrice());
                stmt.setLong(4, order.getId());
                stmt.executeUpdate();
                return order;
            }
        });
    }

    private List<WorkOrder> queryList(String sql, Object param) {
        List<WorkOrder> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, param);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(this.mapToWorkOrder(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при выполнении запроса заказов", e);
        }
        return result;
    }

    private WorkOrder mapToWorkOrder(ResultSet resultSet) throws SQLException {
        return new WorkOrder(
                resultSet.getLong("id"),
                resultSet.getLong("car_id"),
                OrderStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getDouble("total_price")
        );
    }
}
