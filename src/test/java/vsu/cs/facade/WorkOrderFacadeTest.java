package vsu.cs.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.*;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.InvalidStateException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkOrderFacadeTest {

    private AppContainer container;
    private WorkOrderFacade facade;
    private Car car;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
        facade = container.workOrderFacade;
        Client client = container.clientFacade.create("Иван", "+1", "a@a");
        car = container.carFacade.create(client.getId(), "T", "M", "P", 2020);
    }

    @Test
    void createWorkOrder_newStatusAndZeroTotal() {
        WorkOrder order = facade.create(car.getId());
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(0.0, order.getTotalPrice());
    }

    @Test
    void createWorkOrder_missingCar_throwsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> facade.create(999L));
    }

    @Test
    void addItems_recalculatesTotal() {
        WorkOrder order = facade.create(car.getId());
        facade.addItem(order.getId(), "Работа", 1500.0, 1);
        facade.addItem(order.getId(), "Фильтр", 500.0, 2);

        List<WorkOrderItem> items = facade.getItems(order.getId());
        assertEquals(2, items.size());
        assertEquals(2500.0, facade.get(order.getId()).getTotalPrice(), 0.001);
    }

    @Test
    void addItem_toDoneOrder_throwsInvalidState() {
        WorkOrder order = facade.create(car.getId());
        facade.changeStatus(order.getId(), OrderStatus.IN_PROGRESS);
        facade.changeStatus(order.getId(), OrderStatus.DONE);

        assertThrows(InvalidStateException.class,
                () -> facade.addItem(order.getId(), "X", 100.0, 1));
    }

    @Test
    void changeStatus_allowedTransition() {
        WorkOrder order = facade.create(car.getId());
        facade.changeStatus(order.getId(), OrderStatus.IN_PROGRESS);
        facade.changeStatus(order.getId(), OrderStatus.DONE);

        assertEquals(OrderStatus.DONE, facade.get(order.getId()).getStatus());
    }

    @Test
    void deleteWorkOrder_removesItems() {
        WorkOrder order = facade.create(car.getId());
        facade.addItem(order.getId(), "Работа", 100.0, 1);

        facade.delete(order.getId());

        assertThrows(EntityNotFoundException.class, () -> facade.get(order.getId()));
    }
}