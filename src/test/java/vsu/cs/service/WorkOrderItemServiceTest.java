package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.repository.impl.*;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderItemServiceTest {

    private WorkOrderItemService itemService;
    private WorkOrderService orderService;
    private InMemoryWorkOrderRepository orderRepo;

    @BeforeEach
    void setUp() {
        orderRepo = new InMemoryWorkOrderRepository();
        InMemoryWorkOrderItemRepository itemRepo = new InMemoryWorkOrderItemRepository();
        InMemoryCarRepository carRepo = new InMemoryCarRepository();

        itemService = new WorkOrderItemService(itemRepo, orderRepo);
        orderService = new WorkOrderService(orderRepo, carRepo, itemRepo);
    }

    private WorkOrder newOrder() {
        return orderRepo.save(new WorkOrder(1L, OrderStatus.NEW, java.time.LocalDateTime.now(), 0.0));
    }

    @Test
    void addItem_recalculatesTotal() {
        WorkOrder o = newOrder();
        itemService.addItem(o.getId(), "Работа", 1500.0, 1);
        itemService.addItem(o.getId(), "Фильтр", 500.0, 2);

        WorkOrder updated = orderRepo.findById(o.getId()).orElseThrow();
        assertEquals(1500.0 + 1000.0, updated.getTotalPrice(), 0.001);
    }

    @Test
    void removeItem_recalculatesTotal() {
        WorkOrder o = newOrder();
        var i1 = itemService.addItem(o.getId(), "Работа", 1500.0, 1);
        itemService.addItem(o.getId(), "Фильтр", 500.0, 1);

        itemService.removeItem(i1.getId());

        WorkOrder updated = orderRepo.findById(o.getId()).orElseThrow();
        assertEquals(500.0, updated.getTotalPrice(), 0.001);
    }

    @Test
    void addItem_toDoneOrder_throws() {
        WorkOrder o = newOrder();
        o.setStatus(OrderStatus.DONE);
        orderRepo.save(o);

        assertThrows(InvalidStateException.class,
                () -> itemService.addItem(o.getId(), "Работа", 100.0, 1));
    }

    @Test
    void changeStatus_illegalTransition_throws() {
        WorkOrder o = newOrder();
        assertThrows(InvalidStateException.class,
                () -> orderService.changeStatus(o.getId(), OrderStatus.DONE));
    }
}