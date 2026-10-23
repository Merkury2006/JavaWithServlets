package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.repository.impl.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderServiceTest {

    private WorkOrderService orderService;
    private InMemoryWorkOrderRepository orderRepo;
    private InMemoryWorkOrderItemRepository itemRepo;
    private Car car;

    @BeforeEach
    void setUp() {
        orderRepo = new InMemoryWorkOrderRepository();
        itemRepo = new InMemoryWorkOrderItemRepository();
        InMemoryCarRepository carRepo = new InMemoryCarRepository();
        InMemoryClientRepository clientRepo = new InMemoryClientRepository();

        orderService = new WorkOrderService(orderRepo, carRepo, itemRepo);

        Client client = clientRepo.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        car = carRepo.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));
    }

    @Test
    void createWorkOrder_ok() {
        WorkOrder o = orderService.createWorkOrder(car.getId());
        assertNotNull(o.getId());
        assertEquals(OrderStatus.NEW, o.getStatus());
        assertEquals(0.0, o.getTotalPrice());
    }

    @Test
    void createWorkOrder_missingCar_throws() {
        assertThrows(EntityNotFoundException.class, () -> orderService.createWorkOrder(999L));
    }

    @Test
    void getOrdersByCar_ok() {
        orderService.createWorkOrder(car.getId());
        orderService.createWorkOrder(car.getId());
        List<WorkOrder> orders = orderService.getOrdersByCar(car.getId());
        assertEquals(2, orders.size());
    }

    @Test
    void getOrdersByCar_missingCar_throws() {
        assertThrows(EntityNotFoundException.class, () -> orderService.getOrdersByCar(999L));
    }

    @Test
    void getOrdersByStatus_ok() {
        WorkOrder o1 = orderService.createWorkOrder(car.getId());
        orderService.createWorkOrder(car.getId());
        orderService.changeStatus(o1.getId(), OrderStatus.IN_PROGRESS);

        assertEquals(1, orderService.getOrdersByStatus(OrderStatus.IN_PROGRESS).size());
        assertEquals(1, orderService.getOrdersByStatus(OrderStatus.NEW).size());
    }

    @Test
    void changeStatus_newToInProgress_ok() {
        WorkOrder o = orderService.createWorkOrder(car.getId());
        WorkOrder updated = orderService.changeStatus(o.getId(), OrderStatus.IN_PROGRESS);
        assertEquals(OrderStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void changeStatus_sameStatus_noop() {
        WorkOrder o = orderService.createWorkOrder(car.getId());
        WorkOrder updated = orderService.changeStatus(o.getId(), OrderStatus.NEW);
        assertEquals(OrderStatus.NEW, updated.getStatus());
    }

    @Test
    void changeStatus_newToDone_throws() {
        WorkOrder o = orderService.createWorkOrder(car.getId());
        assertThrows(InvalidStateException.class,
                () -> orderService.changeStatus(o.getId(), OrderStatus.DONE));
    }

    @Test
    void changeStatus_fromDone_throws() {
        WorkOrder o = orderService.createWorkOrder(car.getId());
        orderService.changeStatus(o.getId(), OrderStatus.IN_PROGRESS);
        orderService.changeStatus(o.getId(), OrderStatus.DONE);
        assertThrows(InvalidStateException.class,
                () -> orderService.changeStatus(o.getId(), OrderStatus.IN_PROGRESS));
    }

    @Test
    void deleteWorkOrder_cascadesItems() {
        WorkOrder o = orderService.createWorkOrder(car.getId());
        itemRepo.save(new vsu.cs.domain.WorkOrderItem(o.getId(), "Работа", 100.0, 1));
        itemRepo.save(new vsu.cs.domain.WorkOrderItem(o.getId(), "Деталь", 200.0, 1));

        orderService.deleteWorkOrder(o.getId());

        assertFalse(orderRepo.existsById(o.getId()));
        assertTrue(itemRepo.findByWorkOrderId(o.getId()).isEmpty());
    }

    @Test
    void deleteWorkOrder_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> orderService.deleteWorkOrder(999L));
    }
}