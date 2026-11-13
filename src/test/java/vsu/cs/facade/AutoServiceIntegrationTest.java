package vsu.cs.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.*;
import vsu.cs.exception.EntityNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutoServiceIntegrationTest {

    private AppContainer container;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
    }

    @Test
    void fullFlow_createClientCarOrderItems_changeStatus() {
        Client client = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        assertNotNull(client.getId());

        Car car = container.carFacade.create(client.getId(), "Toyota", "Camry", "A123BC", 2020);
        assertNotNull(car.getId());

        WorkOrder order = container.workOrderFacade.create(car.getId());
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(0.0, order.getTotalPrice());

        container.workOrderFacade.addItem(order.getId(), "Работа", 1500.0, 1);
        container.workOrderFacade.addItem(order.getId(), "Фильтр", 500.0, 2);

        List<WorkOrderItem> items = container.workOrderFacade.getItems(order.getId());
        assertEquals(2, items.size());

        assertEquals(2500.0, container.workOrderFacade.get(order.getId()).getTotalPrice(), 0.001);

        container.workOrderFacade.changeStatus(order.getId(), OrderStatus.IN_PROGRESS);
        container.workOrderFacade.changeStatus(order.getId(), OrderStatus.DONE);
        assertEquals(OrderStatus.DONE, container.workOrderFacade.get(order.getId()).getStatus());
    }

    @Test
    void deleteClient_cascadesEverything() {
        Client c = container.clientFacade.create("Иван", "+1", "a@a");
        Car car = container.carFacade.create(c.getId(), "T", "M", "P", 2020);
        WorkOrder o = container.workOrderFacade.create(car.getId());
        container.workOrderFacade.addItem(o.getId(), "Работа", 100.0, 1);

        container.clientFacade.delete(c.getId());

        assertThrows(EntityNotFoundException.class, () -> container.clientFacade.get(c.getId()));
        assertThrows(EntityNotFoundException.class, () -> container.carFacade.get(car.getId()));
        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.get(o.getId()));
    }
}