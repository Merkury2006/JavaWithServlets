package vsu.cs.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Client;
import vsu.cs.repository.impl.InMemoryClientRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryClientRepositoryTest {

    private InMemoryClientRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryClientRepository();
    }

    @Test
    void save_assignsIdWhenNull() {
        Client c = repo.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        assertNotNull(c.getId());
        assertEquals(1L, c.getId());
    }

    @Test
    void save_keepsIdWhenPresent() {
        Client c = repo.save(new Client(42L, "Иван", "+7999", "ivan@mail.ru"));
        assertEquals(42L, c.getId());
        assertTrue(repo.existsById(42L));
    }

    @Test
    void findById_returnsEmptyForMissing() {
        assertTrue(repo.findById(999L).isEmpty());
    }

    @Test
    void findById_returnsClient() {
        Client saved = repo.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        Optional<Client> found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Иван", found.get().getFullName());
    }

    @Test
    void findAll_returnsInInsertionOrder() {
        repo.save(new Client("A", "1", "a@a"));
        repo.save(new Client("B", "2", "b@b"));
        repo.save(new Client("C", "3", "c@c"));
        List<Client> all = repo.findAll();
        assertEquals(3, all.size());
        assertEquals("A", all.get(0).getFullName());
        assertEquals("C", all.get(2).getFullName());
    }

    @Test
    void deleteById_removes() {
        Client saved = repo.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        repo.deleteById(saved.getId());
        assertFalse(repo.existsById(saved.getId()));
    }

    @Test
    void update_overwritesExisting() {
        Client saved = repo.save(new Client("Иван", "+7999", "ivan@mail.ru"));
        saved.setFullName("Пётр");
        repo.save(saved);
        assertEquals("Пётр", repo.findById(saved.getId()).orElseThrow().getFullName());
    }
}