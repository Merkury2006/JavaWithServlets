package vsu.cs.dto.console.request;

public record AddItemRequest(Long workOrderId, String description, double price, int quantity) {}