package vsu.cs.dto.request;

public record CreateCarRequest(Long clientId, String brand, String model, String plateNumber, int year) {}