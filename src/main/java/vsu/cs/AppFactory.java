package vsu.cs;

import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.facade.CarFacade;
import vsu.cs.facade.ClientFacade;
import vsu.cs.facade.WorkOrderFacade;
import vsu.cs.repository.CarRepository;
import vsu.cs.repository.ClientRepository;
import vsu.cs.repository.WorkOrderItemRepository;
import vsu.cs.repository.WorkOrderRepository;
import vsu.cs.repository.impl.inmemory.InMemoryCarRepository;
import vsu.cs.repository.impl.inmemory.InMemoryClientRepository;
import vsu.cs.repository.impl.inmemory.InMemoryWorkOrderItemRepository;
import vsu.cs.repository.impl.inmemory.InMemoryWorkOrderRepository;
import vsu.cs.repository.impl.jdbc.JdbcCarRepository;
import vsu.cs.repository.impl.jdbc.JdbcClientRepository;
import vsu.cs.repository.impl.jdbc.JdbcWorkOrderItemRepository;
import vsu.cs.repository.impl.jdbc.JdbcWorkOrderRepository;
import vsu.cs.service.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public final class AppFactory {
    private AppFactory() {}

    public static AppContainer createInMemory() {
        ClientRepository clientRepository = new InMemoryClientRepository();
        CarRepository carRepository = new InMemoryCarRepository();
        WorkOrderRepository orderRepository = new InMemoryWorkOrderRepository();
        WorkOrderItemRepository itemRepository = new InMemoryWorkOrderItemRepository();

        WorkOrderRepository cascadingOrderRepository = createCascadingOrderRepository(orderRepository, itemRepository);
        CarRepository cascadingCarRepository = createCascadingCarRepository(carRepository, cascadingOrderRepository);
        ClientRepository cascadingClientRepository = createCascadingClientRepository(clientRepository, cascadingCarRepository);
        WorkOrderItemRepository recalculatingItemRepository = createRecalculatingItemRepository(itemRepository, cascadingOrderRepository);

        return createContainer(cascadingClientRepository, cascadingCarRepository, cascadingOrderRepository, recalculatingItemRepository);
    }

    public static AppContainer createJdbc(DataSource dataSource) {
        ClientRepository clientRepository = new JdbcClientRepository(dataSource);
        CarRepository carRepository = new JdbcCarRepository(dataSource);
        WorkOrderRepository orderRepository = new JdbcWorkOrderRepository(dataSource);
        WorkOrderItemRepository itemRepository = new JdbcWorkOrderItemRepository(dataSource);

        return createContainer(clientRepository, carRepository, orderRepository, itemRepository);
    }

    private static AppContainer createContainer(ClientRepository clientRepository, CarRepository carRepository,
                                                WorkOrderRepository orderRepository, WorkOrderItemRepository itemRepository) {
        ClientService clientService = new ClientService(clientRepository);
        CarService carService = new CarService(carRepository, clientRepository);
        WorkOrderService orderService = new WorkOrderService(orderRepository, carRepository);
        WorkOrderItemService itemService = new WorkOrderItemService(itemRepository, orderRepository);

        ClientFacade clientFacade = new ClientFacade(clientService);
        CarFacade carFacade = new CarFacade(carService);
        WorkOrderFacade workOrderFacade = new WorkOrderFacade(orderService, itemService);

        return new AppContainer(clientFacade, carFacade, workOrderFacade);
    }

    private static ClientRepository createCascadingClientRepository(ClientRepository delegate, CarRepository carRepository) {
        return new ClientRepository() {
            @Override public Client save(Client client) { return delegate.save(client); }
            @Override public Optional<Client> findById(Long id) { return delegate.findById(id); }
            @Override public List<Client> findAll() { return delegate.findAll(); }
            @Override public boolean existsById(Long id) { return delegate.existsById(id); }

            @Override
            public boolean deleteById(Long id) {
                for (Car car : carRepository.findByClientId(id)) {
                    carRepository.deleteById(car.getId());
                }
                return delegate.deleteById(id);
            }
        };
    }

    private static CarRepository createCascadingCarRepository(CarRepository delegate, WorkOrderRepository orderRepository) {
        return new CarRepository() {
            @Override public Car save(Car car) { return delegate.save(car); }
            @Override public Optional<Car> findById(Long id) { return delegate.findById(id); }
            @Override public List<Car> findAll() { return delegate.findAll(); }
            @Override public List<Car> findByClientId(Long id) { return delegate.findByClientId(id); }
            @Override public boolean existsById(Long id) { return delegate.existsById(id); }

            @Override
            public boolean deleteById(Long id) {
                for (WorkOrder order : orderRepository.findByCarId(id)) {
                    orderRepository.deleteById(order.getId());
                }
                return delegate.deleteById(id);
            }
        };
    }

    private static WorkOrderRepository createCascadingOrderRepository(WorkOrderRepository delegate, WorkOrderItemRepository itemRepository) {
        return new WorkOrderRepository() {
            @Override public WorkOrder save(WorkOrder order) { return delegate.save(order); }
            @Override public Optional<WorkOrder> findById(Long id) { return delegate.findById(id); }
            @Override public List<WorkOrder> findAll() { return delegate.findAll(); }
            @Override public List<WorkOrder> findByCarId(Long id) { return delegate.findByCarId(id); }
            @Override public List<WorkOrder> findByStatus(OrderStatus status) { return delegate.findByStatus(status); }
            @Override public boolean existsById(Long id) { return delegate.existsById(id); }

            @Override
            public boolean deleteById(Long id) {
                itemRepository.deleteByWorkOrderId(id);
                return delegate.deleteById(id);
            }
        };
    }

    private static WorkOrderItemRepository createRecalculatingItemRepository(WorkOrderItemRepository delegate, WorkOrderRepository orderRepository) {
        return new WorkOrderItemRepository() {
            @Override public Optional<WorkOrderItem> findById(Long id) { return delegate.findById(id); }
            @Override public List<WorkOrderItem> findAll() { return delegate.findAll(); }
            @Override public List<WorkOrderItem> findByWorkOrderId(Long id) { return delegate.findByWorkOrderId(id); }
            @Override public boolean existsById(Long id) { return delegate.existsById(id); }
            @Override public boolean deleteByWorkOrderId(Long id) { return delegate.deleteByWorkOrderId(id); }

            @Override
            public WorkOrderItem save(WorkOrderItem item) {
                WorkOrderItem saved = delegate.save(item);
                this.recalculateTotal(item.getWorkOrderId(), orderRepository, delegate);
                return saved;
            }

            @Override
            public boolean deleteById(Long id) {
                Optional<WorkOrderItem> item = delegate.findById(id);
                boolean deleted = delegate.deleteById(id);
                if (deleted) {
                    item.ifPresent(i -> this.recalculateTotal(i.getWorkOrderId(), orderRepository, delegate));
                }
                return deleted;
            }

            private void recalculateTotal(Long workOrderId, WorkOrderRepository orderRepository, WorkOrderItemRepository itemRepository) {
                orderRepository.findById(workOrderId).ifPresent(order -> {
                    double total = itemRepository.findByWorkOrderId(workOrderId).stream()
                            .mapToDouble(WorkOrderItem::getSubtotal)
                            .sum();
                    order.setTotalPrice(total);
                    orderRepository.save(order);
                });
            }
        };
    }
}
