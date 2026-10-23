package vsu.cs.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppFactory;
import vsu.cs.domain.OrderStatus;
import vsu.cs.dto.request.*;
import vsu.cs.dto.response.*;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.exception.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AutoServiceFacadeTest {

    private AutoServiceFacade facade;

    @BeforeEach
    void setUp() {
        facade = AppFactory.createInMemoryAutoServiceFacade();
    }

    @Test
    void fullFlow_createClientCarOrderItems_changeStatus() {
        ClientResponse client = facade.createClient(
                new CreateClientRequest("Иван", "+7999", "ivan@mail.ru"));
        assertNotNull(client.id());

        CarResponse car = facade.createCar(
                new CreateCarRequest(client.id(), "Toyota", "Camry", "A123BC", 2020));
        assertNotNull(car.id());

        WorkOrderResponse order = facade.createWorkOrder(
                new CreateWorkOrderRequest(car.id()));
        assertEquals(OrderStatus.NEW, order.status());
        assertEquals(0.0, order.totalPrice());

        facade.addItem(new AddItemRequest(order.id(), "Работа", 1500.0, 1));
        facade.addItem(new AddItemRequest(order.id(), "Фильтр", 500.0, 2));

        List<WorkOrderItemResponse> items = facade.getItems(order.id());
        assertEquals(2, items.size());

        WorkOrderResponse afterItems = facade.getWorkOrder(order.id());
        assertEquals(2500.0, afterItems.totalPrice(), 0.001);

        facade.changeStatus(new ChangeStatusRequest(order.id(), OrderStatus.IN_PROGRESS));
        facade.changeStatus(new ChangeStatusRequest(order.id(), OrderStatus.DONE));
        assertEquals(OrderStatus.DONE, facade.getWorkOrder(order.id()).status());
    }

    @Test
    void createClient_blankName_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> facade.createClient(new CreateClientRequest("", "+7999", "x@x")));
    }

    @Test
    void getClient_missing_throwsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> facade.getClient(999L));
    }

    @Test
    void addItem_toDoneOrder_throwsInvalidState() {
        ClientResponse c = facade.createClient(new CreateClientRequest("Иван", "+1", "a@a"));
        CarResponse car = facade.createCar(new CreateCarRequest(c.id(), "T", "M", "P", 2020));
        WorkOrderResponse o = facade.createWorkOrder(new CreateWorkOrderRequest(car.id()));
        facade.changeStatus(new ChangeStatusRequest(o.id(), OrderStatus.IN_PROGRESS));
        facade.changeStatus(new ChangeStatusRequest(o.id(), OrderStatus.DONE));

        assertThrows(InvalidStateException.class,
                () -> facade.addItem(new AddItemRequest(o.id(), "X", 100.0, 1)));
    }

    @Test
    void deleteClient_cascadesEverything() {
        ClientResponse c = facade.createClient(new CreateClientRequest("Иван", "+1", "a@a"));
        CarResponse car = facade.createCar(new CreateCarRequest(c.id(), "T", "M", "P", 2020));
        WorkOrderResponse o = facade.createWorkOrder(new CreateWorkOrderRequest(car.id()));
        facade.addItem(new AddItemRequest(o.id(), "Работа", 100.0, 1));

        facade.deleteClient(c.id());

        assertThrows(EntityNotFoundException.class, () -> facade.getClient(c.id()));
        assertThrows(EntityNotFoundException.class, () -> facade.getCar(car.id()));
        assertThrows(EntityNotFoundException.class, () -> facade.getWorkOrder(o.id()));
    }

    @Test
    void getAllClients_returnsEmptyInitially() {
        assertTrue(facade.getAllClients().isEmpty());
    }

    @Test
    void updateClient_changesFields() {
        ClientResponse c = facade.createClient(new CreateClientRequest("Иван", "+1", "a@a"));
        ClientResponse updated = facade.updateClient(
                new UpdateClientRequest(c.id(), "Пётр", "+2", "b@b"));
        assertEquals("Пётр", updated.fullName());
        assertEquals("+2", updated.phone());
    }
}