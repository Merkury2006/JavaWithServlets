package vsu.cs.domain;

import java.util.Objects;

public class WorkOrderItem implements Identifiable<Long> {
    private Long id;
    private Long workOrderId;
    private String description;
    private double price;
    private int quantity;

    public WorkOrderItem(Long id, Long workOrderId, String description, double price, int quantity) {
        this.id = id;
        this.workOrderId = workOrderId;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public WorkOrderItem(Long workOrderId, String description, double price, int quantity) {
        this(null, workOrderId, description, price, quantity);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() { return price * quantity; }

    @Override
    public String toString() {
        return "WorkOrderItem{id=" + id + ", workOrderId=" + workOrderId
                + ", description='" + description + "', price=" + price
                + ", qty=" + quantity + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        WorkOrderItem that = (WorkOrderItem) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}