package vsu.cs.repository.impl;

import vsu.cs.domain.WorkOrderItem;
import vsu.cs.repository.WorkOrderItemRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryWorkOrderItemRepository extends InMemoryRepository<WorkOrderItem, Long> implements WorkOrderItemRepository {
    public InMemoryWorkOrderItemRepository() {
        super(new AtomicLong(0)::incrementAndGet);
    }

    @Override
    public List<WorkOrderItem> findByWorkOrderId(Long id) {
        return this.storage.values().stream().filter(
                workOrderItem -> Objects.equals(workOrderItem.getWorkOrderId(), id)
        ).toList();
    }

    @Override
    public void deleteByWorkOrderId(Long id) {
        this.storage.values().removeIf(workOrderItem -> Objects.equals(workOrderItem.getWorkOrderId(), id));
    }
}
