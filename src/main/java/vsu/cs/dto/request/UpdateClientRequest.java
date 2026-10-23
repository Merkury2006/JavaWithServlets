package vsu.cs.dto.request;

public record UpdateClientRequest(Long id, String fullName, String phone, String email) {
}
