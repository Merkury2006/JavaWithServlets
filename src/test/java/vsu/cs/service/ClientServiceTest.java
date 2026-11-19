package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class ClientServiceTest {
    private AppContainer container;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
    }

    @Test
    void createClient_ok() {
        Client c = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        assertNotNull(c.getId());
        assertEquals("Иван", c.getFullName());
    }

    @Test
    void createClient_blankName_throws() {
        assertThrows(ValidationException.class,
                () -> container.clientFacade.create("", "+7999", "ivan@mail.ru"));
    }

    @Test
    void getClient_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.clientFacade.get(999L));
    }

    @Test
    void deleteClient_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.clientFacade.delete(999L));
    }

    @Test
    void deleteClient_cascade() {
        Client c = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        container.clientFacade.delete(c.getId());

        assertThrows(EntityNotFoundException.class, () -> container.clientFacade.get(c.getId()));
    }
}