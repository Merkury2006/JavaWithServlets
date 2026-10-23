package vsu.cs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.ValidationException;
import vsu.cs.repository.impl.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CarServiceTest {

    private CarService carService;
    private InMemoryCarRepository carRepo;
    private Client client;
    private Client another;

    @BeforeEach
    void setUp() {
        carRepo = new InMemoryCarRepository();
        InMemoryClientRepository clientRepo = new InMemoryClientRepository();
        InMemoryWorkOrderRepository orderRepo = new InMemoryWorkOrderRepository();
        InMemoryWorkOrderItemRepository itemRepo = new InMemoryWorkOrderItemRepository();
        carService = new CarService(carRepo, clientRepo, orderRepo, itemRepo);
        client = clientRepo.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        another = clientRepo.save(new Client("Пётр", "+2", "b@b"));
    }

    @Test
    void createCar_ok() {
        Car c = carService.createCar(client.getId(), "Toyota", "Camry", "A123BC", 2020);
        assertNotNull(c.getId());
        assertEquals("Toyota", c.getBrand());
        assertEquals(client.getId(), c.getClientId());
    }

    @Test
    void createCar_missingClient_throws() {
        assertThrows(EntityNotFoundException.class,
                () -> carService.createCar(999L, "Toyota", "Camry", "A123BC", 2020));
    }

    @Test
    void createCar_blankBrand_throws() {
        assertThrows(ValidationException.class,
                () -> carService.createCar(client.getId(), "", "Camry", "A123BC", 2020));
    }

    @Test
    void createCar_blankModel_throws() {
        assertThrows(ValidationException.class,
                () -> carService.createCar(client.getId(), "Toyota", "  ", "A123BC", 2020));
    }

    @Test
    void createCar_badYear_throws() {
        assertThrows(ValidationException.class,
                () -> carService.createCar(client.getId(), "Toyota", "Camry", "A123BC", 1800));
    }

    @Test
    void getCarsByClient_returnsOnlyMatching() {
        carService.createCar(client.getId(), "Toyota", "Camry", "A1", 2020);
        carService.createCar(client.getId(), "BMW", "X5", "B2", 2021);
        carService.createCar(another.getId(), "Kia", "Rio", "C3", 2019);

        List<Car> cars = carService.getCarsByClient(client.getId());
        assertEquals(2, cars.size());
        assertTrue(cars.stream().allMatch(c -> c.getClientId().equals(client.getId())));
    }

    @Test
    void getCarsByClient_missingClient_throws() {
        assertThrows(EntityNotFoundException.class, () -> carService.getCarsByClient(999L));
    }

    @Test
    void getCar_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> carService.getCar(999L));
    }

    @Test
    void deleteCar_removes() {
        Car c = carService.createCar(client.getId(), "Toyota", "Camry", "A1", 2020);
        carService.deleteCar(c.getId());
        assertFalse(carRepo.existsById(c.getId()));
    }

    @Test
    void deleteCar_missing_throws() {
        assertThrows(EntityNotFoundException.class, () -> carService.deleteCar(999L));
    }
}