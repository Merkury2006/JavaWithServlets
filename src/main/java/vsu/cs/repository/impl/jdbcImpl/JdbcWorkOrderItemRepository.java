package vsu.cs.repository.impl.jdbcImpl;

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
        String sql = "SELECT id, work_order_id, description, price, quantity FROM work_order_items WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "SELECT id, work_order_id, description, price, quantity FROM work_order_items ORDER BY id";
        List<WorkOrderItem> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
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
        String sql = "SELECT id, work_order_id, description, price, quantity FROM work_order_items WHERE work_order_id = ? ORDER BY id";
        List<WorkOrderItem> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String deleteSql = "DELETE FROM work_order_items WHERE id = ?";
        String selectSql = "SELECT work_order_id FROM work_order_items WHERE id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            Long workOrderId = null;
            try (PreparedStatement findStatement = connection.prepareStatement(selectSql)) {
                findStatement.setLong(1, id);
                try (ResultSet resultSet = findStatement.executeQuery()) {
                    if (resultSet.next()) {
                        workOrderId = resultSet.getLong("work_order_id");
                    }
                }
            }

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
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
        String sql = "DELETE FROM work_order_items WHERE work_order_id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
        String sql = "SELECT 1 FROM work_order_items WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Ошибка при проверке существования позиции заказа: " + id, e);
        }
    }

    private WorkOrderItem insert(WorkOrderItem item) {
        String sql = "INSERT INTO work_order_items (work_order_id, description, price, quantity) VALUES (?, ?, ?, ?)";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        String sql = "UPDATE work_order_items SET work_order_id = ?, description = ?, price = ?, quantity = ? WHERE id = ?";
        return JdbcUtils.runInTransaction(dataSource, connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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
        String updateSql = """
            UPDATE work_orders 
            SET total_price = (SELECT COALESCE(SUM(price * quantity), 0) FROM work_order_items WHERE work_order_id = ?)
            WHERE id = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
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