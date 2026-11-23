package vsu.cs.repository.impl.jdbc;

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

    private static final String FIND_BY_ID = "SELECT id, car_id, status, created_at, total_price FROM work_orders WHERE id = ?";
    private static final String FIND_ALL = "SELECT id, car_id, status, created_at, total_price FROM work_orders ORDER BY id";
    private static final String FIND_BY_CAR_ID = "SELECT id, car_id, status, created_at, total_price FROM work_orders WHERE car_id = ? ORDER BY id";
    private static final String FIND_BY_STATUS = "SELECT id, car_id, status, created_at, total_price FROM work_orders WHERE status = ? ORDER BY id";
    private static final String DELETE_BY_ID = "DELETE FROM work_orders WHERE id = ?";
    private static final String EXISTS_BY_ID = "SELECT 1 FROM work_orders WHERE id = ?";
    private static final String INSERT = "INSERT INTO work_orders (car_id, status, created_at, total_price) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE work_orders SET car_id = ?, status = ?, total_price = ? WHERE id = ?";


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
        return this.queryList(FIND_BY_CAR_ID, carId);
    }

    @Override
    public List<WorkOrder> findByStatus(OrderStatus status) {
        return this.queryList(FIND_BY_STATUS, status.name());
    }

    @Override
    public Optional<WorkOrder> findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
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
        List<WorkOrder> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL)) {
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
            throw new RepositoryException("Ошибка при проверке существования заказа: " + id, e);
        }
    }

    private WorkOrder insert(WorkOrder order) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
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
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
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
