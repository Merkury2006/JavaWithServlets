package vsu.cs.dto;

import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.dto.response.CarResponse;
import vsu.cs.dto.response.ClientResponse;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperTest {

    @Test
    void clientToResponse() {
        Client c = new Client(1L, "Иван", "+7999", "ivan@mail.ru");
        ClientResponse r = DtoMapper.toResponse(c);
        assertEquals(1L, r.id());
        assertEquals("Иван", r.fullName());
    }

    @Test
    void carToResponse() {
        Car c = new Car(2L, 1L, "Toyota", "Camry", "A123BC", 2020);
        CarResponse r = DtoMapper.toResponse(c);
        assertEquals(2L, r.id());
        assertEquals(1L, r.clientId());
        assertEquals("Toyota", r.brand());
    }
}