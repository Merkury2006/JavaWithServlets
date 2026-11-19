package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.InvalidStateException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkOrderItemServiceTest {
    private AppContainer container;
    private WorkOrder order;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
        Client client = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        Car car = container.carFacade.create(client.getId(), "Toyota", "Camry", "A1", 2020);
        order = container.workOrderFacade.create(car.getId());
    }

    @Test
    void addItem_recalculatesTotal() {
        container.workOrderFacade.addItem(order.getId(), "Работа", 1500.0, 1);
        container.workOrderFacade.addItem(order.getId(), "Фильтр", 500.0, 2);

        WorkOrder updated = container.workOrderFacade.get(order.getId());
        assertEquals(1500.0 + 1000.0, updated.getTotalPrice(), 0.001);
    }

    @Test
    void removeItem_recalculatesTotal() {
        var i1 = container.workOrderFacade.addItem(order.getId(), "Работа", 1500.0, 1);
        container.workOrderFacade.addItem(order.getId(), "Фильтр", 500.0, 1);

        container.workOrderFacade.removeItem(i1.getId());

        WorkOrder updated = container.workOrderFacade.get(order.getId());
        assertEquals(500.0, updated.getTotalPrice(), 0.001);
    }

    @Test
    void addItem_toDoneOrder_throws() {
        container.workOrderFacade.changeStatus(order.getId(), OrderStatus.IN_PROGRESS);
        container.workOrderFacade.changeStatus(order.getId(), OrderStatus.DONE);

        assertThrows(InvalidStateException.class,
                () -> container.workOrderFacade.addItem(order.getId(), "Работа", 100.0, 1));
    }

    @Test
    void changeStatus_illegalTransition_throws() {
        assertThrows(InvalidStateException.class,
                () -> container.workOrderFacade.changeStatus(order.getId(), OrderStatus.DONE));
    }
}