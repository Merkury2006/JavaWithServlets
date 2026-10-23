package vsu.cs;

import vsu.cs.facade.AutoServiceFacade;
import vsu.cs.repository.CarRepository;
import vsu.cs.repository.ClientRepository;
import vsu.cs.repository.WorkOrderItemRepository;
import vsu.cs.repository.WorkOrderRepository;
import vsu.cs.repository.impl.InMemoryCarRepository;
import vsu.cs.repository.impl.InMemoryClientRepository;
import vsu.cs.repository.impl.InMemoryWorkOrderItemRepository;
import vsu.cs.repository.impl.InMemoryWorkOrderRepository;
import vsu.cs.service.*;

public final class AppFactory {
    private AppFactory() {}

    public static AutoServiceFacade createInMemoryAutoServiceFacade() {
        ClientRepository clientRepository = new InMemoryClientRepository();
        CarRepository carRepository = new InMemoryCarRepository();
        WorkOrderRepository orderRepository = new InMemoryWorkOrderRepository();
        WorkOrderItemRepository itemRepository = new InMemoryWorkOrderItemRepository();

        ClientService clientService = new ClientService(clientRepository, carRepository, orderRepository, itemRepository);
        CarService carService = new CarService(carRepository, clientRepository, orderRepository, itemRepository);
        WorkOrderService orderService = new WorkOrderService(orderRepository, carRepository, itemRepository);
        WorkOrderItemService itemService = new WorkOrderItemService(itemRepository, orderRepository);

        return new AutoServiceFacade(clientService, carService, orderService, itemService);
    }
}
