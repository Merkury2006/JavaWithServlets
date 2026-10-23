package vsu.cs.dto.response;

import vsu.cs.domain.OrderStatus;
import java.time.LocalDateTime;

public record WorkOrderResponse(Long id, Long carId, OrderStatus status, LocalDateTime createdAt, double totalPrice) {
    @Override
    public String toString() {
        return "#" + id + " car #" + carId + " [" + status + "] "
                + createdAt.toLocalDate() + " total=" + totalPrice;
    }
}