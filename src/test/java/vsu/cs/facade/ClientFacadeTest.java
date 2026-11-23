package vsu.cs.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppFactory;
import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class ClientFacadeTest {

    private ClientFacade facade;

    @BeforeEach
    void setUp() {
        facade = AppFactory.createInMemory().clientFacade;
    }

    @Test
    void createClient_returnsClientWithId() {
        Client client = facade.create("Иван", "+7999", "ivan@mail.ru");
        assertNotNull(client.getId());
        assertEquals("Иван", client.getFullName());
    }

    @Test
    void createClient_blankName_throwsValidation() {
        assertThrows(ValidationException.class,
                () -> facade.create("", "+7999", "x@x"));
    }

    @Test
    void getClient_missing_throwsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> facade.get(999L));
    }

    @Test
    void getAllClients_returnsEmptyInitially() {
        assertTrue(facade.getAll().isEmpty());
    }

    @Test
    void updateClient_changesFields() {
        Client c = facade.create("Иван", "+1", "a@a");
        Client updated = facade.update(c.getId(), "Пётр", "+2", "b@b");
        assertEquals("Пётр", updated.getFullName());
        assertEquals("+2", updated.getPhone());
    }

    @Test
    void deleteClient_cascadesEverything() {
        Client c = facade.create("Иван", "+1", "a@a");
        facade.delete(c.getId());

        assertThrows(EntityNotFoundException.class, () -> facade.get(c.getId()));
    }
}