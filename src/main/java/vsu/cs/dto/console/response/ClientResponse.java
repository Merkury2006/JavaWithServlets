package vsu.cs.dto.console.response;

public record ClientResponse(Long id, String fullName, String phone, String email) {
    @Override
    public String toString() {
        return "#" + id + " " + fullName + " (" + phone + ", " + email + ")";
    }
}