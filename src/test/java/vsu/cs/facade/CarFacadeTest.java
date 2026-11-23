package vsu.cs.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.AppContainer;
import vsu.cs.AppFactory;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class CarFacadeTest {

    private AppContainer container;
    private CarFacade facade;

    @BeforeEach
    void setUp() {
        container = AppFactory.createInMemory();
        facade = container.carFacade;
    }

    @Test
    void createCar_returnsCarWithId() {
        Client client = container.clientFacade.create("Иван", "+1", "a@a");
        Car car = facade.create(client.getId(), "Toyota", "Camry", "A123BC", 2020);

        assertNotNull(car.getId());
        assertEquals("Toyota", car.getBrand());
        assertEquals(client.getId(), car.getClientId());
    }

    @Test
    void createCar_missingClient_throwsNotFound() {
        assertThrows(EntityNotFoundException.class,
                () -> facade.create(999L, "T", "M", "P", 2020));
    }

    @Test
    void createCar_blankBrand_throwsValidation() {
        Client client = container.clientFacade.create("Иван", "+1", "a@a");
        assertThrows(ValidationException.class,
                () -> facade.create(client.getId(), "", "M", "P", 2020));
    }

    @Test
    void getCarsByClient_returnsOnlyOwnCars() {
        Client a = container.clientFacade.create("A", "+1", "a@a");
        Client b = container.clientFacade.create("B", "+2", "b@b");
        facade.create(a.getId(), "T", "M", "P1", 2020);
        facade.create(b.getId(), "T", "M", "P2", 2020);

        assertEquals(1, facade.getByClient(a.getId()).size());
        assertEquals(1, facade.getByClient(b.getId()).size());
    }

    @Test
    void getCar_missing_throwsNotFound() {
        assertThrows(EntityNotFoundException.class, () -> facade.get(999L));
    }

    @Test
    void deleteCar_removesCar() {
        Client c = container.clientFacade.create("Иван", "+1", "a@a");
        Car car = facade.create(c.getId(), "T", "M", "P", 2020);

        facade.delete(car.getId());

        assertThrows(EntityNotFoundException.class, () -> facade.get(car.getId()));
    }
}