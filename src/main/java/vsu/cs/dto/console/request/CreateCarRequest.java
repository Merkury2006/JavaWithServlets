package vsu.cs.dto.console.request;

public record CreateCarRequest(Long clientId, String brand, String model, String plateNumber, int year) {}