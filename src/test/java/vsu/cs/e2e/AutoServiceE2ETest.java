package vsu.cs.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.repository.impl.jdbc.BaseJdbcIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoServiceE2ETest extends BaseJdbcIntegrationTest {
    private AppContainer container;

    @BeforeEach
    void setUp() {
        super.cleanDatabase();
        this.container = AppFactory.createJdbc(dataSource);
    }

    @Test
    void fullFlow_createClientCarOrderAddItemsAndChangeStatus() {
        Client client = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        assertNotNull(client.getId());

        Car car = container.carFacade.create(client.getId(), "Toyota", "Camry", "A1", 2020);
        assertNotNull(car.getId());

        WorkOrder order = container.workOrderFacade.create(car.getId());
        assertEquals(OrderStatus.NEW, order.getStatus());
        assertEquals(0.0, order.getTotalPrice());

        container.workOrderFacade.addItem(order.getId(), "Работа", 1500.0, 1);
        container.workOrderFacade.addItem(order.getId(), "Фильтр", 500.0, 2);

        WorkOrder updated = container.workOrderFacade.get(order.getId());
        assertEquals(2500.0, updated.getTotalPrice(), 0.001);

        container.workOrderFacade.changeStatus(order.getId(), OrderStatus.IN_PROGRESS);
        container.workOrderFacade.changeStatus(order.getId(), OrderStatus.DONE);

        assertEquals(OrderStatus.DONE, container.workOrderFacade.get(order.getId()).getStatus());
    }

    @Test
    void fullFlow_deleteClientCascadesEverything() {
        Client client = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        Car car = container.carFacade.create(client.getId(), "Toyota", "Camry", "A1", 2020);
        WorkOrder order = container.workOrderFacade.create(car.getId());
        container.workOrderFacade.addItem(order.getId(), "Работа", 1500.0, 1);

        container.clientFacade.delete(client.getId());

        assertThrows(EntityNotFoundException.class, () -> container.clientFacade.get(client.getId()));
        assertThrows(EntityNotFoundException.class, () -> container.carFacade.getByClient(client.getId()));
        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.get(order.getId()));
        assertThrows(EntityNotFoundException.class, () -> container.workOrderFacade.getItems(order.getId()));
    }
}
