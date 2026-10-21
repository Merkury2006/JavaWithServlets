package vsu.cs.repository.impl;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.repository.WorkOrderRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryWorkOrderRepository extends InMemoryRepository<WorkOrder, Long> implements WorkOrderRepository {
    public InMemoryWorkOrderRepository() {
        super(new AtomicLong(0)::incrementAndGet);
    }

    @Override
    public List<WorkOrder> findByCarId(Long id) {
        return this.storage.values().stream().filter(order -> Objects.equals(order.getCarId(), id)).toList();
    }

    @Override
    public List<WorkOrder> findByStatus(OrderStatus status) {
        return this.storage.values().stream().filter(order -> order.getStatus().equals(status)).toList();
    }
}
