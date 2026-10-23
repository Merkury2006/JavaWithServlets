package vsu.cs.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class WorkOrder implements Identifiable<Long> {
    private Long id;
    private Long carId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private double totalPrice;

    public WorkOrder(Long id, Long carId, OrderStatus status, LocalDateTime createdAt, double totalPrice) {
        this.id = id;
        this.carId = carId;
        this.status = status;
        this.createdAt = createdAt;
        this.totalPrice = totalPrice;
    }

    public WorkOrder(Long carId, OrderStatus status, LocalDateTime createdAt, double totalPrice) {
      this(null, carId, status, createdAt, totalPrice);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCarId() { return carId; }
    public void setCarId(Long carId) { this.carId = carId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    @Override
    public String toString() {
        return "WorkOrder{id=" + id + ", carId=" + carId + ", status=" + status
                + ", createdAt=" + createdAt + ", totalPrice=" + totalPrice + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WorkOrder workOrder = (WorkOrder) object;
        return Objects.equals(id, workOrder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}