package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.InvalidStateException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkOrderServiceTest {
    private AppContainer container;
    private Car car;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
        Client client = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        car = container.carFacade.create(client.getId(), "Toyota", "Camry", "A1", 2020);
    }

    @Test
    void createWorkOrder_ok() {
        WorkOrder o = container.workOrderFacade.create(car.getId());
        assertNotNull(o.getId());
        assertEquals(OrderStatus.NEW, o.getStatus());
        assertEquals(0.0, o.getTotalPrice());
    }

    @Test
    void createWorkOrder_missingCar_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.create(999L));
    }

    @Test
    void getOrdersByCar_ok() {
        container.workOrderFacade.create(car.getId());
        container.workOrderFacade.create(car.getId());
        List<WorkOrder> orders = container.workOrderFacade.getByCar(car.getId());
        assertEquals(2, orders.size());
    }

    @Test
    void getOrdersByCar_missingCar_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.getByCar(999L));
    }

    @Test
    void getOrdersByStatus_ok() {
        WorkOrder o1 = container.workOrderFacade.create(car.getId());
        container.workOrderFacade.create(car.getId());
        container.workOrderFacade.changeStatus(o1.getId(), OrderStatus.IN_PROGRESS);

        assertEquals(1, container.workOrderFacade.getByStatus(OrderStatus.IN_PROGRESS).size());
        assertEquals(1, container.workOrderFacade.getByStatus(OrderStatus.NEW).size());
    }

    @Test
    void changeStatus_newToInProgress_ok() {
        WorkOrder o = container.workOrderFacade.create(car.getId());
        WorkOrder updated = container.workOrderFacade.changeStatus(o.getId(), OrderStatus.IN_PROGRESS);
        assertEquals(OrderStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void changeStatus_sameStatus_noop() {
        WorkOrder o = container.workOrderFacade.create(car.getId());
        WorkOrder updated = container.workOrderFacade.changeStatus(o.getId(), OrderStatus.NEW);
        assertEquals(OrderStatus.NEW, updated.getStatus());
    }

    @Test
    void changeStatus_newToDone_throws() {
        WorkOrder o = container.workOrderFacade.create(car.getId());
        assertThrows(InvalidStateException.class,
                () -> container.workOrderFacade.changeStatus(o.getId(), OrderStatus.DONE));
    }

    @Test
    void changeStatus_fromDone_throws() {
        WorkOrder o = container.workOrderFacade.create(car.getId());
        container.workOrderFacade.changeStatus(o.getId(), OrderStatus.IN_PROGRESS);
        container.workOrderFacade.changeStatus(o.getId(), OrderStatus.DONE);
        assertThrows(InvalidStateException.class,
                () -> container.workOrderFacade.changeStatus(o.getId(), OrderStatus.IN_PROGRESS));
    }

    @Test
    void deleteWorkOrder_cascadesItems() {
        WorkOrder o = container.workOrderFacade.create(car.getId());
        container.workOrderFacade.addItem(o.getId(), "Работа", 100.0, 1);
        container.workOrderFacade.addItem(o.getId(), "Деталь", 200.0, 1);

        container.workOrderFacade.delete(o.getId());

        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.get(o.getId()));
    }

    @Test
    void deleteWorkOrder_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.delete(999L));
    }
}