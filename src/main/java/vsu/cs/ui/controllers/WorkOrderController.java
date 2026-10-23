package vsu.cs.ui.controllers;

import vsu.cs.domain.OrderStatus;
import vsu.cs.dto.request.AddItemRequest;
import vsu.cs.dto.request.ChangeStatusRequest;
import vsu.cs.dto.request.CreateWorkOrderRequest;
import vsu.cs.dto.response.WorkOrderItemResponse;
import vsu.cs.dto.response.WorkOrderResponse;
import vsu.cs.facade.AutoServiceFacade;
import vsu.cs.ui.ConsoleIO;

import java.util.List;

public class WorkOrderController {
    private final AutoServiceFacade facade;
    private final ConsoleIO io;

    public WorkOrderController(AutoServiceFacade facade, ConsoleIO io) {
        this.facade = facade;
        this.io = io;
    }

    public void run() {
        while (true) {
            System.out.println("""
                    
                    --- Work orders ---
                    1. List all
                    2. List by car
                    3. List by status
                    4. Create for car
                    5. Show items
                    6. Add item
                    7. Remove item
                    8. Change status
                    9. Delete order (cascade items)
                    0. Back
                    """);
            String choice = io.readLine("> ").trim();
            switch (choice) {
                case "1" -> io.safe(this::listAll);
                case "2" -> io.safe(this::listByCar);
                case "3" -> io.safe(this::listByStatus);
                case "4" -> io.safe(this::create);
                case "5" -> io.safe(this::showItems);
                case "6" -> io.safe(this::addItem);
                case "7" -> io.safe(this::removeItem);
                case "8" -> io.safe(this::changeStatus);
                case "9" -> io.safe(this::delete);
                case "0" -> { return; }
                default -> System.out.println("Unknown command.");
            }
        }
    }

    private void listAll() {
        this.printOrders(facade.getAllWorkOrders());
    }

    private void listByCar() {
        Long carId = io.readLong("Car id: ");
        this.printOrders(facade.getOrdersByCar(carId));
    }

    private void listByStatus() {
        OrderStatus status = io.readStatus();
        this.printOrders(facade.getOrdersByStatus(status));
    }

    private void printOrders(List<WorkOrderResponse> orders) {
        if (orders.isEmpty()) {
            System.out.println("(no work orders)");
            return;
        }
        orders.forEach(System.out::println);
    }

    private void create() {
        Long carId = io.readLong("Car id: ");
        WorkOrderResponse response = facade.createWorkOrder(new CreateWorkOrderRequest(carId));
        System.out.println("Created: " + response);
    }

    private void showItems() {
        Long workOrderId = io.readLong("Work order id: ");
        List<WorkOrderItemResponse> items = facade.getItems(workOrderId);
        if (items.isEmpty()) {
            System.out.println("(no items)");
            return;
        }
        items.forEach(System.out::println);
        double total = items.stream().mapToDouble(WorkOrderItemResponse::subtotal).sum();
        System.out.println("Total sum: " + total);
    }

    private void addItem() {
        Long workOrderId = io.readLong("Work order id: ");
        String description = io.readLine("Description: ");
        double price = io.readDouble("Price: ");
        int quantity = io.readInt("Quantity: ");
        WorkOrderItemResponse response = facade.addItem(new AddItemRequest(workOrderId, description, price, quantity));
        System.out.println("Added: " + response);
    }

    private void removeItem() {
        Long itemId = io.readLong("Item id: ");
        facade.removeItem(itemId);
        System.out.println("Removed item " + itemId + ".");
    }

    private void changeStatus() {
        Long workOrderId = io.readLong("Work order id: ");
        OrderStatus status = io.readStatus();
        WorkOrderResponse response = facade.changeStatus(new ChangeStatusRequest(workOrderId, status));
        System.out.println("Updated: " + response);
    }

    private void delete() {
        Long id = io.readLong("Work order id: ");
        facade.deleteWorkOrder(id);
        System.out.println("Deleted work order " + id + " (with items).");
    }
}