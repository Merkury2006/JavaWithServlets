package vsu.cs.repository;

import vsu.cs.domain.WorkOrderItem;

import java.util.List;

public interface WorkOrderItemRepository extends Repository<WorkOrderItem, Long> {
    List<WorkOrderItem> findByWorkOrderId(Long id);
    void deleteByWorkOrderId(Long id);
}
