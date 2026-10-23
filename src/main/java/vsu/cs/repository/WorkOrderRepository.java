package vsu.cs.repository;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;

import java.util.List;

public interface WorkOrderRepository extends Repository<WorkOrder, Long> {
    List<WorkOrder> findByCarId(Long id);
    List<WorkOrder> findByStatus(OrderStatus status);
}
