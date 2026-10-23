package vsu.cs.dto.response;

public record WorkOrderItemResponse(Long id, Long workOrderId, String description, double price, int quantity, double subtotal) {
    @Override
    public String toString() {
        return "#" + id + " " + description + " x" + quantity
                + " x " + price + " = " + subtotal;
    }
}