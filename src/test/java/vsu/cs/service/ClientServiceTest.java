package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.ValidationException;
import vsu.cs.repository.impl.*;

import static org.junit.jupiter.api.Assertions.*;

class ClientServiceTest {

    private ClientService service;
    private InMemoryClientRepository clientRepo;
    private InMemoryCarRepository carRepo;

    @BeforeEach
    void setUp() {
        clientRepo = new InMemoryClientRepository();
        carRepo = new InMemoryCarRepository();
        InMemoryWorkOrderRepository orderRepo = new InMemoryWorkOrderRepository();
        InMemoryWorkOrderItemRepository itemRepo = new InMemoryWorkOrderItemRepository();
        service = new ClientService(clientRepo, carRepo, orderRepo, itemRepo);
    }

    @Test
    void createClient_ok() {
        Client c = service.createClient("Иван", "+7999", "ivan@mail.ru");
        assertNotNull(c.getId());
        assertEquals("Иван", c.getFullName());
    }

    @Test
    void createClient_blankName_throws() {
        assertThrows(ValidationException.class,
                () -> service.createClient("", "+7999", "ivan@mail.ru"));
    }

    @Test
    void getClient_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.getClient(999L));
    }

    @Test
    void deleteClient_cascade() {
        Client c = service.createClient("Иван", "+7999", "ivan@mail.ru");
        service.deleteClient(c.getId());
        assertFalse(clientRepo.existsById(c.getId()));
    }

    @Test
    void deleteClient_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> service.deleteClient(999L));
    }
}