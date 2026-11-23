package vsu.cs.repository.impl.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.domain.Client;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcClientRepositoryTest extends BaseJdbcIntegrationTest{
    private JdbcClientRepository repository;
    private JdbcCarRepository carRepository;

    @BeforeEach
    void setUp() {
        super.cleanDatabase();
        this.repository = new JdbcClientRepository(dataSource);
        this.carRepository = new JdbcCarRepository(dataSource);
    }

    @Test
    void saveAndFindById() {
        Client client = new Client("Иван", "+7999", "ivan@mail.ru");
        repository.save(client);

        assertNotNull(client.getId());
        Optional<Client> loaded = repository.findById(client.getId());
        assertTrue(loaded.isPresent());
        assertEquals("Иван", loaded.get().getFullName());
        assertEquals("+7999", loaded.get().getPhone());
        assertEquals("ivan@mail.ru", loaded.get().getEmail());
    }

    @Test
    void update() {
        Client client = new Client("Иван", "+7999", "ivan@mail.ru");
        repository.save(client);

        client.setFullName("Пётр");
        client.setPhone("+2");
        repository.save(client);

        Client loaded = repository.findById(client.getId()).orElseThrow();
        assertEquals("Пётр", loaded.getFullName());
        assertEquals("+2", loaded.getPhone());
    }

    @Test
    void findAll() {
        repository.save(new Client("Иван", "+1", "a@a"));
        repository.save(new Client("Пётр", "+2", "b@b"));

        List<Client> clients = repository.findAll();
        assertEquals(2, clients.size());
    }



    @Test
    void existsById() {
        Client client = new Client("Иван", "+7999", "ivan@mail.ru");
        repository.save(client);

        assertTrue(repository.existsById(client.getId()));
        assertFalse(repository.existsById(999L));
    }

    @Test
    void deleteById() {
        Client client = new Client("Иван", "+7999", "ivan@mail.ru");
        repository.save(client);

        assertTrue(repository.deleteById(client.getId()));
        assertFalse(repository.findById(client.getId()).isPresent());
        assertFalse(repository.deleteById(client.getId()));
    }

    @Test
    void deleteById_cascadesCars() {
        Client client = repository.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        carRepository.save(new Car(client.getId(), "Toyota", "Camry", "A1", 2020));

        assertFalse(carRepository.findByClientId(client.getId()).isEmpty());

        assertTrue(repository.deleteById(client.getId()));

        assertTrue(carRepository.findByClientId(client.getId()).isEmpty());
    }
}
