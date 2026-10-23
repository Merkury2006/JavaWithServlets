package vsu.cs.dto.response;

public record CarResponse(Long id, Long clientId, String brand, String model, String plateNumber, int year) {
    @Override
    public String toString() {
        return "#" + id + " " + brand + " " + model + " " + year + " [" + plateNumber + "], client #" + clientId;
    }
}
