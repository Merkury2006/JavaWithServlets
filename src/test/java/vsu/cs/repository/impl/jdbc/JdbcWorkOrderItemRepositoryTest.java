package vsu.cs.repository.impl.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcWorkOrderItemRepositoryTest extends BaseJdbcIntegrationTest {
    private JdbcClientRepository clientRepository;
    private JdbcCarRepository carRepository;
    private JdbcWorkOrderRepository orderRepository;
    private JdbcWorkOrderItemRepository itemRepository;

    private Client client;
    private Car car;
    private WorkOrder order;

    @BeforeEach
    void setUp() {
        super.cleanDatabase();
        this.clientRepository = new JdbcClientRepository(dataSource);
        this.carRepository = new JdbcCarRepository(dataSource);
        this.orderRepository = new JdbcWorkOrderRepository(dataSource);
        this.itemRepository = new JdbcWorkOrderItemRepository(dataSource);

        this.client = clientRepository.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        this.car = carRepository.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));
        this.order = orderRepository.save(new WorkOrder(car.getId(), OrderStatus.NEW, LocalDateTime.now(), 0.0));
    }

    @Test
    void addItem_recalculatesTotalPriceInDatabase() {
        itemRepository.save(new WorkOrderItem(order.getId(), "Работа", 1500.0, 1));
        itemRepository.save(new WorkOrderItem(order.getId(), "Фильтр", 500.0, 2));

        WorkOrder updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(2500.0, updated.getTotalPrice(), 0.001);
    }

    @Test
    void removeItem_recalculatesTotalPriceInDatabase() {
        WorkOrderItem item1 = itemRepository.save(new WorkOrderItem(order.getId(), "Работа", 1500.0, 1));
        itemRepository.save(new WorkOrderItem(order.getId(), "Фильтр", 500.0, 1));

        itemRepository.deleteById(item1.getId());

        WorkOrder updated = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(500.0, updated.getTotalPrice(), 0.001);
    }

    @Test
    void deleteByWorkOrderId_removesAllItems() {
        itemRepository.save(new WorkOrderItem(order.getId(), "Работа", 1500.0, 1));
        itemRepository.save(new WorkOrderItem(order.getId(), "Фильтр", 500.0, 1));

        orderRepository.deleteById(order.getId());

        assertTrue(itemRepository.findByWorkOrderId(order.getId()).isEmpty());
    }

    @Test
    void deleteOrder_cascadesItemsInDatabase() {
        itemRepository.save(new WorkOrderItem(order.getId(), "Работа", 1500.0, 1));

        clientRepository.deleteById(client.getId());

        assertTrue(carRepository.findByClientId(client.getId()).isEmpty());
        assertTrue(orderRepository.findByCarId(car.getId()).isEmpty());
        assertTrue(itemRepository.findByWorkOrderId(order.getId()).isEmpty());
    }
}
