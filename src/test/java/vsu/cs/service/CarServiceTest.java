package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.ValidationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CarServiceTest {
    private AppContainer container;
    private Client client;
    private Client another;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
        client = container.clientFacade.create("Иван", "+7999", "ivan@mail.ru");
        another = container.clientFacade.create("Пётр", "+2", "b@b");
    }

    @Test
    void createCar_ok() {
        Car c = container.carFacade.create(client.getId(), "Toyota", "Camry", "A123BC", 2020);
        assertNotNull(c.getId());
        assertEquals("Toyota", c.getBrand());
        assertEquals(client.getId(), c.getClientId());
    }

    @Test
    void createCar_missingClient_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> container.carFacade.create(999L, "Toyota", "Camry", "A123BC", 2020));
    }

    @Test
    void createCar_blankBrand_throws() {
        assertThrows(ValidationException.class,
                () -> container.carFacade.create(client.getId(), "", "Camry", "A123BC", 2020));
    }

    @Test
    void createCar_blankModel_throws() {
        assertThrows(ValidationException.class,
                () -> container.carFacade.create(client.getId(), "Toyota", "  ", "A123BC", 2020));
    }

    @Test
    void createCar_badYear_throws() {
        assertThrows(ValidationException.class,
                () -> container.carFacade.create(client.getId(), "Toyota", "Camry", "A123BC", 1800));
    }

    @Test
    void getCarsByClient_returnsOnlyMatching() {
        container.carFacade.create(client.getId(), "Toyota", "Camry", "A1", 2020);
        container.carFacade.create(client.getId(), "BMW", "X5", "B2", 2021);
        container.carFacade.create(another.getId(), "Kia", "Rio", "C3", 2019);

        List<Car> cars = container.carFacade.getByClient(client.getId());
        assertEquals(2, cars.size());
        assertTrue(cars.stream().allMatch(c -> c.getClientId().equals(client.getId())));
    }

    @Test
    void getCarsByClient_missingClient_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.carFacade.getByClient(999L));
    }

    @Test
    void getCar_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.carFacade.get(999L));
    }

    @Test
    void deleteCar_removes() {
        Car c = container.carFacade.create(client.getId(), "Toyota", "Camry", "A1", 2020);
        container.carFacade.delete(c.getId());
        assertThrows(EntityNotFoundException.class, () -> container.carFacade.get(c.getId()));
    }

    @Test
    void deleteCar_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> container.carFacade.delete(999L));
    }
}