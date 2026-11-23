package vsu.cs.facade;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.service.WorkOrderItemService;
import vsu.cs.service.WorkOrderService;

import java.util.List;

public final class WorkOrderFacade {
    private final WorkOrderService orderService;
    private final WorkOrderItemService itemService;

    public WorkOrderFacade(WorkOrderService orderService, WorkOrderItemService itemService) {
        this.orderService = orderService;
        this.itemService = itemService;
    }

    public WorkOrder create(Long carId) {
        return orderService.createWorkOrder(carId);
    }

    public List<WorkOrder> getByCar(Long carId) {
        return orderService.getOrdersByCar(carId);
    }

    public List<WorkOrder> getByStatus(OrderStatus status) {
        return orderService.getOrdersByStatus(status);
    }

    public List<WorkOrder> getAll() {
        return orderService.getAllWorkOrders();
    }

    public WorkOrder get(Long id) {
        return orderService.getWorkOrder(id);
    }

    public void delete(Long id) {
        orderService.deleteWorkOrder(id);
    }

    public WorkOrder changeStatus(Long id, OrderStatus newStatus) {
        return orderService.changeStatus(id, newStatus);
    }

    public WorkOrderItem addItem(Long workOrderId, String description, double price, int quantity) {
        return itemService.addItem(workOrderId, description, price, quantity);
    }

    public void removeItem(Long itemId) {
        itemService.removeItem(itemId);
    }

    public List<WorkOrderItem> getItems(Long workOrderId) {
        return itemService.getItems(workOrderId);
    }
}