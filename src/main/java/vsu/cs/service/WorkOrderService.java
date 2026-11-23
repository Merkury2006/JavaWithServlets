package vsu.cs.service;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.repository.CarRepository;
import vsu.cs.repository.WorkOrderItemRepository;
import vsu.cs.repository.WorkOrderRepository;
import vsu.cs.utils.Validator;

import java.time.LocalDateTime;
import java.util.List;

public class WorkOrderService {
    private final WorkOrderRepository workOrderRepository;
    private final CarRepository carRepository;
    private final WorkOrderItemRepository workOrderItemRepository;

    public WorkOrderService(WorkOrderRepository orderRepository, CarRepository carRepository, WorkOrderItemRepository itemRepository) {
        this.workOrderRepository = orderRepository;
        this.carRepository = carRepository;
        this.workOrderItemRepository = itemRepository;
    }

    public WorkOrder createWorkOrder(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new EntityNotFoundException("Car not found: " + carId);
        }
        WorkOrder order = new WorkOrder(carId, OrderStatus.NEW, LocalDateTime.now(), 0.0);
        return workOrderRepository.save(order);
    }

    public List<WorkOrder> getOrdersByCar(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new EntityNotFoundException("Car not found: " + carId);
        }
        return workOrderRepository.findByCarId(carId);
    }

    public List<WorkOrder> getOrdersByStatus(OrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    public WorkOrder getWorkOrder(Long id) {
        return workOrderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Work order not found: " + id)
        );
    }

    public void deleteWorkOrder(Long id) {
        if (!workOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("Work order not found: " + id);
        }
        workOrderItemRepository.deleteByWorkOrderId(id);
        workOrderRepository.deleteById(id);
    }

    public WorkOrder changeStatus(Long workOrderId, OrderStatus newStatus) {
        WorkOrder order = workOrderRepository.findById(workOrderId).orElseThrow(
                () -> new EntityNotFoundException("Work order not found: " + workOrderId)
        );

        if (order.getStatus() == newStatus) {
            return order;
        }

        Validator.requireAllowedTransitionChangeStatus(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        return workOrderRepository.save(order);
    }
}
