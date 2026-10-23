package vsu.cs.dto.request;

public record AddItemRequest(Long workOrderId, String description, double price, int quantity) {}