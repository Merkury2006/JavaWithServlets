package vsu.cs.service;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.repository.WorkOrderItemRepository;
import vsu.cs.repository.WorkOrderRepository;
import vsu.cs.utils.Validator;

import java.util.List;

public class WorkOrderItemService {
    private final WorkOrderItemRepository workOrderItemRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderItemService(WorkOrderItemRepository workOrderItemRepository, WorkOrderRepository workOrderRepository) {
        this.workOrderItemRepository = workOrderItemRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public WorkOrderItem addItem(Long workOrderId, String description, double price, int quantity) {
        WorkOrder order = workOrderRepository.findById(workOrderId).orElseThrow(
                () -> new EntityNotFoundException("Work order not found: " + workOrderId)
        );

        Validator.requireWorkOrderEditable(order);
        Validator.requireNotBlank(description, "Item description");
        Validator.requirePositive(price, "Item price");
        Validator.requirePositive(quantity, "Item quantity");

        WorkOrderItem item = workOrderItemRepository.save(new WorkOrderItem(workOrderId, description, price, quantity));
        this.recalculateTotal(order.getId());
        return item;
    }

    public void removeItem(Long itemId) {
        WorkOrderItem item = workOrderItemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException("Item not found: " + itemId)
        );
        WorkOrder order = workOrderRepository.findById(item.getWorkOrderId()).orElseThrow(
                () -> new EntityNotFoundException("Order of item not found: " + item.getWorkOrderId())
        );

        Validator.requireWorkOrderEditable(order);

        workOrderItemRepository.deleteById(itemId);
        this.recalculateTotal(order.getId());
    }

    public List<WorkOrderItem> getItems(Long workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw new EntityNotFoundException("Work order not found: " + workOrderId);
        }
        return workOrderItemRepository.findByWorkOrderId(workOrderId);
    }

    private void recalculateTotal(Long workOrderId) {
        WorkOrder order = workOrderRepository.findById(workOrderId).orElseThrow(
                () -> new EntityNotFoundException("Order not found: " + workOrderId)
        );
        double total = workOrderItemRepository.findByWorkOrderId(workOrderId).stream()
                .mapToDouble(WorkOrderItem::getSubtotal)
                .sum();
        order.setTotalPrice(total);
        workOrderRepository.save(order);
    }

}
