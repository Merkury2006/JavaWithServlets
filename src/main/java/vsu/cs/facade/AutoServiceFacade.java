package vsu.cs.facade;

import vsu.cs.domain.*;
import vsu.cs.service.CarService;
import vsu.cs.service.ClientService;
import vsu.cs.service.WorkOrderItemService;
import vsu.cs.service.WorkOrderService;

import java.util.List;

public final class AutoServiceFacade {
    private final ClientService clientService;
    private final CarService carService;
    private final WorkOrderService orderService;
    private final WorkOrderItemService itemService;

    public AutoServiceFacade(ClientService clientService, CarService carService, WorkOrderService orderService, WorkOrderItemService itemService) {
        this.clientService = clientService;
        this.carService = carService;
        this.orderService = orderService;
        this.itemService = itemService;
    }

    public Client createClient(String fullName, String phone, String email) {
        return clientService.createClient(fullName, phone, email);
    }
    public Client updateClient(Long id, String fullName, String phone, String email) {
        return clientService.updateClient(id, fullName, phone, email);
    }
    public List<Client> getAllClients() { return clientService.getAllClients(); }
    public Client getClient(Long id) { return clientService.getClient(id); }
    public void deleteClient(Long id) { clientService.deleteClient(id); }


    public Car createCar(Long clientId, String brand, String model, String plateNumber, int year) {
        return carService.createCar(clientId, brand, model, plateNumber, year);
    }
    public List<Car> getCarsByClient(Long clientId) { return carService.getCarsByClient(clientId); }
    public List<Car> getAllCars() { return carService.getAllCars(); }
    public Car getCar(Long id) { return carService.getCar(id); }
    public void deleteCar(Long id) { carService.deleteCar(id); }


    public WorkOrder createWorkOrder(Long carId) { return orderService.createWorkOrder(carId); }
    public List<WorkOrder> getOrdersByCar(Long carId) { return orderService.getOrdersByCar(carId); }
    public List<WorkOrder> getOrdersByStatus(OrderStatus status) { return orderService.getOrdersByStatus(status); }
    public List<WorkOrder> getAllWorkOrders() { return orderService.getAllWorkOrders(); }
    public WorkOrder getWorkOrder(Long id) { return orderService.getWorkOrder(id); }
    public void deleteWorkOrder(Long id) { orderService.deleteWorkOrder(id); }
    public WorkOrder changeStatus(Long workOrderId, OrderStatus newStatus) {
        return orderService.changeStatus(workOrderId, newStatus);
    }

    public WorkOrderItem addItem(Long workOrderId, String description, double price, int quantity) {
        return itemService.addItem(workOrderId, description, price, quantity);
    }
    public void removeItem(Long itemId) { itemService.removeItem(itemId); }
    public List<WorkOrderItem> getItems(Long workOrderId) { return itemService.getItems(workOrderId); }
}

