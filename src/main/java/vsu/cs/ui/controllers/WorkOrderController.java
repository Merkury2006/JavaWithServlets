package vsu.cs.ui.controllers;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.dto.console.DtoMapper;
import vsu.cs.dto.console.response.WorkOrderItemResponse;
import vsu.cs.dto.console.response.WorkOrderResponse;
import vsu.cs.facade.WorkOrderFacade;
import vsu.cs.ui.ConsoleIO;

import java.util.List;

public class WorkOrderController {
    private final WorkOrderFacade facade;
    private final ConsoleIO io;

    public WorkOrderController(WorkOrderFacade facade, ConsoleIO io) {
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
        this.printOrders(facade.getAll().stream().map(DtoMapper::toResponse).toList());
    }

    private void listByCar() {
        Long carId = io.readLong("Car id: ");
        this.printOrders(facade.getByCar(carId).stream().map(DtoMapper::toResponse).toList());
    }

    private void listByStatus() {
        OrderStatus status = io.readStatus();
        this.printOrders(facade.getByStatus(status).stream().map(DtoMapper::toResponse).toList());
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
        WorkOrder created = facade.create(carId);
        System.out.println("Created: " + DtoMapper.toResponse(created));
    }

    private void showItems() {
        Long workOrderId = io.readLong("Work order id: ");
        List<WorkOrderItemResponse> items = facade.getItems(workOrderId).stream().map(DtoMapper::toResponse).toList();
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
        WorkOrderItem added = facade.addItem(workOrderId, description, price, quantity);
        System.out.println("Added: " + DtoMapper.toResponse(added));
    }

    private void removeItem() {
        Long itemId = io.readLong("Item id: ");
        facade.removeItem(itemId);
        System.out.println("Removed item " + itemId + ".");
    }

    private void changeStatus() {
        Long workOrderId = io.readLong("Work order id: ");
        OrderStatus status = io.readStatus();
        WorkOrder changed = facade.changeStatus(workOrderId, status);
        System.out.println("Updated: " + DtoMapper.toResponse(changed));
    }

    private void delete() {
        Long id = io.readLong("Work order id: ");
        facade.delete(id);
        System.out.println("Deleted work order " + id + " (with items).");
    }
}