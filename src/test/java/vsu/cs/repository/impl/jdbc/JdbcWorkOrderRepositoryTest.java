package vsu.cs.repository.impl.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.exception.RepositoryException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcWorkOrderRepositoryTest extends BaseJdbcIntegrationTest {
    private JdbcClientRepository clientRepository;
    private JdbcCarRepository carRepository;
    private JdbcWorkOrderRepository repository;
    private JdbcWorkOrderItemRepository itemRepository;
    private Car car;

    @BeforeEach
    void setUp() {
        super.cleanDatabase();
        this.clientRepository = new JdbcClientRepository(dataSource);
        this.carRepository = new JdbcCarRepository(dataSource);
        this.repository = new JdbcWorkOrderRepository(dataSource);
        this.itemRepository = new JdbcWorkOrderItemRepository(dataSource);

        Client client = clientRepository.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        this.car = carRepository.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));
    }

    @Test
    void saveAndFindById() {
        WorkOrder order = new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0);
        repository.save(order);

        assertNotNull(order.getId());
        WorkOrder loaded = repository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.NEW, loaded.getStatus());
        assertEquals(car.getId(), loaded.getCarId());
    }

    @Test
    void updateStatus() {
        WorkOrder order = new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0);
        repository.save(order);

        order.setStatus(OrderStatus.IN_PROGRESS);
        repository.save(order);

        WorkOrder loaded = repository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.IN_PROGRESS, loaded.getStatus());
    }

    @Test
    void findByCarId() {
        repository.save(new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0));
        repository.save(new WorkOrder(car.getId(), OrderStatus.IN_PROGRESS, LocalDateTime.now(), 0.0));

        List<WorkOrder> orders = repository.findByCarId(car.getId());
        assertEquals(2, orders.size());
    }

    @Test
    void findByStatus() {
        repository.save(new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0));
        repository.save(new WorkOrder(car.getId(), OrderStatus.IN_PROGRESS, LocalDateTime.now(), 0.0));

        List<WorkOrder> newOrders = repository.findByStatus(OrderStatus.NEW);
        assertEquals(1, newOrders.size());
        assertEquals(OrderStatus.NEW, newOrders.getFirst().getStatus());
    }

    @Test
    void findAll() {
        repository.save(new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0));
        repository.save(new WorkOrder(car.getId(), OrderStatus.IN_PROGRESS, LocalDateTime.now(), 0.0));

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void save_missingCar_throwsRepositoryException() {
        WorkOrder order = new WorkOrder(999L, OrderStatus.NEW, LocalDateTime.now(), 0.0);
        assertThrows(RepositoryException.class, () -> repository.save(order));
    }

    @Test
    void deleteById() {
        WorkOrder order = new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0);
        repository.save(order);

        assertTrue(repository.deleteById(order.getId()));
        assertFalse(repository.findById(order.getId()).isPresent());
    }

    @Test
    void deleteById_cascadesItems() {
        WorkOrder order = new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0);
        repository.save(order);

        itemRepository.save(new WorkOrderItem(order.getId(), "Работа", 1500.0, 1));
        itemRepository.save(new WorkOrderItem(order.getId(), "Фильтр", 500.0, 1));

        assertEquals(2, itemRepository.findByWorkOrderId(order.getId()).size());

        assertTrue(repository.deleteById(order.getId()));
        assertFalse(repository.findById(order.getId()).isPresent());

        assertTrue(itemRepository.findByWorkOrderId(order.getId()).isEmpty());
    }
}
