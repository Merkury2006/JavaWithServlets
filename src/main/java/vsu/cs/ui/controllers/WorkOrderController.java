package vsu.cs.ui.controllers;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.dto.console.DtoMapper;
import vsu.cs.dto.console.response.WorkOrderItemResponse;
import vsu.cs.dto.console.response.WorkOrderResponse;
import vsu.cs.facade.WorkOrderFacade;
import vsu.cs.ui.ConsoleIO;
import vsu.cs.ui.Menu;

import java.util.List;

public class WorkOrderController {
    private final WorkOrderFacade facade;
    private final ConsoleIO io;

    public WorkOrderController(WorkOrderFacade facade, ConsoleIO io) {
        this.facade = facade;
        this.io = io;
    }

    public void run() {
        new Menu(io, "Work orders")
                .add("all","List all", this::listAll)
                .add("by-car","List by car", this::listByCar)
                .add("by-status","List by status", this::listByStatus)
                .add("create","Create for car", this::create)
                .add("items","Show items", this::showItems)
                .add("add-item","Add item", this::addItem)
                .add("remove-item","Remove item", this::removeItem)
                .add("change-status","Change status", this::changeStatus)
                .add("delete","Delete order (cascade)", this::delete)
                .run();
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