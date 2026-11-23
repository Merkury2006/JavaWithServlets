package vsu.cs.repository.impl.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.RepositoryException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcCarRepositoryTest extends BaseJdbcIntegrationTest{
    private JdbcClientRepository clientRepository;
    private JdbcWorkOrderRepository orderRepository;
    private JdbcCarRepository repository;
    private Client client;

    @BeforeEach
    void setUp() {
        super.cleanDatabase();
        this.clientRepository = new JdbcClientRepository(dataSource);
        this.orderRepository = new JdbcWorkOrderRepository(dataSource);
        this.repository = new JdbcCarRepository(dataSource);
        this.client = clientRepository.save(new Client("Иван", "+7999", "ivan@mail.ru"));
    }

    @Test
    void saveAndFindById() {
        Car car = new Car(client.getId(), "Toyota", "Camry", "A1", 2020);
        repository.save(car);

        assertNotNull(car.getId());
        Car loaded = repository.findById(car.getId()).orElseThrow();
        assertEquals("Toyota", loaded.getBrand());
        assertEquals("Camry", loaded.getModel());
        assertEquals(client.getId(), loaded.getClientId());
    }

    @Test
    void update() {
        Car car = new Car(client.getId(), "Toyota", "Camry", "A1", 2020);
        repository.save(car);

        car.setBrand("BMW");
        car.setYear(2022);
        repository.save(car);

        Car loaded = repository.findById(car.getId()).orElseThrow();
        assertEquals("BMW", loaded.getBrand());
        assertEquals(2022, loaded.getYear());
    }

    @Test
    void findByClientId() {
        repository.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));
        repository.save(new Car(client.getId(), "BMW", "X5", "B2", 2021));

        Client another = clientRepository.save(new Client("Пётр", "+2", "b@b"));
        repository.save(new Car(another.getId(), "Kia", "Rio", "C3", 2019));

        List<Car> cars = repository.findByClientId(client.getId());
        assertEquals(2, cars.size());
    }

    @Test
    void findAll() {
        repository.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));
        repository.save(new Car(client.getId(), "BMW", "X5", "B2", 2021));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void save_missingClient_throwsRepositoryException() {
        Car car = new Car(999L, "Toyota", "Camry", "A1", 2020);
        assertThrows(RepositoryException.class, () -> repository.save(car));
    }

    @Test
    void deleteById() {
        Car car = new Car(client.getId(), "Toyota", "Camry", "A1", 2020);
        repository.save(car);

        assertTrue(repository.deleteById(car.getId()));
        assertFalse(repository.findById(car.getId()).isPresent());
    }

    @Test
    void deleteById_cascadesOrders() {
        Car car = repository.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));
        orderRepository.save(new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0));

        assertFalse(orderRepository.findByCarId(car.getId()).isEmpty());

        assertTrue(repository.deleteById(car.getId()));

        assertTrue(orderRepository.findByCarId(car.getId()).isEmpty());
    }
}
