package vsu.cs.dto.request;

import vsu.cs.domain.OrderStatus;

public record ChangeStatusRequest(Long workOrderId, OrderStatus newStatus) {}