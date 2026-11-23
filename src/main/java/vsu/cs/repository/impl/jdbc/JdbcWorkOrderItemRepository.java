package vsu.cs.repository.impl.jdbc;

import vsu.cs.db.transaction.JdbcUtils;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.exception.RepositoryException;
import vsu.cs.repository.WorkOrderItemRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkOrderItemRepository implements WorkOrderItemRepository {
    private final DataSource dataSource;

    private static final String FIND_BY_ID = "SELECT id, work_order_id, description, price, quantity FROM work_order_items WHERE id = ?";
    private static final String FIND_ALL = "SELECT id, work_order_id, description, price, quantity FROM work_order_items ORDER BY id";
    private static final String FIND_BY_WORK_ORDER_ID = "SELECT id, work_order_id, description, price, quantity FROM work_order_items WHERE work_order_id = ? ORDER BY id";
    private static final String FIND_WORK_ORDER_ID_BY_ITEM_ID = "SELECT work_order_id FROM work_order_items WHERE id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM work_order_items WHERE id = ?";
    private static final String DELETE_BY_WORK_ORDER_ID = "DELETE FROM work_order_items WHERE work_order_id = ?";
    private static final String EXISTS_BY_ID = "SELECT 1 FROM work_order_items WHERE id = ?";
    private static final String INSERT = "INSERT INTO work_order_items (work_order_id, description, price, quantity) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE work_order_items SET work_order_id = ?, description = ?, price = ?, quantity = ? WHERE id = ?";
    private static final String UPDATE_TOTAL_PRICE = """
        UPDATE work_orders
        SET total_price = (SELECT COALESCE(SUM(price * quantity), 0) FROM work_order_items WHERE work_order_id = ?)
        WHERE id = ?
        """;


    public JdbcWorkOrderItemRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public WorkOrderItem save(WorkOrderItem item) {
        if (item.getId() == null) {
            return this.insert(item);
        }
        return this.update(item);
    }

    @Override
    public Optional<WorkOrderItem> findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(this.mapToWorkOrderItem(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при поиске позиции заказа по id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<WorkOrderItem> findAll() {
        List<WorkOrderItem> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL)) {
            while (resultSet.next()) {
                result.add(this.mapToWorkOrderItem(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении списка позиций заказов", e);
        }
        return result;
    }

    @Override
    public List<WorkOrderItem> findByWorkOrderId(Long workOrderId) {
        List<WorkOrderItem> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_WORK_ORDER_ID)) {
            statement.setLong(1, workOrderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(this.mapToWorkOrderItem(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при получении позиций заказа: " + workOrderId, e);
        }
        return result;
    }

    @Override
    public boolean deleteById(Long id) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            Long workOrderId = null;
            try (PreparedStatement findStatement = connection.prepareStatement(FIND_WORK_ORDER_ID_BY_ITEM_ID)) {
                findStatement.setLong(1, id);
                try (ResultSet resultSet = findStatement.executeQuery()) {
                    if (resultSet.next()) {
                        workOrderId = resultSet.getLong("work_order_id");
                    }
                }
            }

            try (PreparedStatement deleteStatement = connection.prepareStatement(DELETE_BY_ID)) {
                deleteStatement.setLong(1, id);
                boolean deleted = deleteStatement.executeUpdate() > 0;
                if (deleted && workOrderId != null) {
                    this.recalculateTotal(connection, workOrderId);
                }
                return deleted;
            }
        });
    }

    @Override
    public boolean deleteByWorkOrderId(Long workOrderId) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_WORK_ORDER_ID)) {
                statement.setLong(1, workOrderId);
                boolean deleted = statement.executeUpdate() > 0;
                if (deleted) {
                    this.recalculateTotal(connection, workOrderId);
                }
                return deleted;
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
            throw new RepositoryException("Ошибка при проверке существования позиции заказа: " + id, e);
        }
    }

    private WorkOrderItem insert(WorkOrderItem item) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, item.getWorkOrderId());
                stmt.setString(2, item.getDescription());
                stmt.setDouble(3, item.getPrice());
                stmt.setInt(4, item.getQuantity());
                stmt.executeUpdate();
                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        item.setId(resultSet.getLong(1));
                    }
                }
                this.recalculateTotal(connection, item.getWorkOrderId());
                return item;
            }
        });
    }

    private WorkOrderItem update(WorkOrderItem item) {
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
                stmt.setLong(1, item.getWorkOrderId());
                stmt.setString(2, item.getDescription());
                stmt.setDouble(3, item.getPrice());
                stmt.setInt(4, item.getQuantity());
                stmt.setLong(5, item.getId());
                stmt.executeUpdate();
            }
            this.recalculateTotal(connection, item.getWorkOrderId());
            return item;
        });
    }

    private void recalculateTotal(Connection connection, Long workOrderId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_TOTAL_PRICE)) {
            stmt.setLong(1, workOrderId);
            stmt.setLong(2, workOrderId);
            stmt.executeUpdate();
        }
    }

    private WorkOrderItem mapToWorkOrderItem(ResultSet resultSet) throws SQLException {
        return new WorkOrderItem(
                resultSet.getLong("id"),
                resultSet.getLong("work_order_id"),
                resultSet.getString("description"),
                resultSet.getDouble("price"),
                resultSet.getInt("quantity")
        );
    }
}