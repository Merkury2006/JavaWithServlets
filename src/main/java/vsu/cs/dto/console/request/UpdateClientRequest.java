package vsu.cs.dto.console.request;

public record UpdateClientRequest(Long id, String fullName, String phone, String email) {
}
