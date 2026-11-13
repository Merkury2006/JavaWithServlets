package vsu.cs.dto.console.request;

import vsu.cs.domain.OrderStatus;

public record ChangeStatusRequest(Long workOrderId, OrderStatus newStatus) {}