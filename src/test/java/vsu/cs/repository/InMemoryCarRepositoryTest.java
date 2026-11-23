package vsu.cs.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.Car;
import vsu.cs.repository.impl.InMemoryCarRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCarRepositoryTest {

    private InMemoryCarRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryCarRepository();
    }

    @Test
    void findByClientId_returnsOnlyMatching() {
        repo.save(new Car(1L, "Toyota", "Camry", "A1", 2020));
        repo.save(new Car(1L, "BMW", "X5", "B2", 2021));
        repo.save(new Car(2L, "Kia", "Rio", "C3", 2019));

        List<Car> cars = repo.findByClientId(1L);
        assertEquals(2, cars.size());
        assertTrue(cars.stream().allMatch(c -> c.getClientId().equals(1L)));
    }

    @Test
    void findByClientId_noMatch_returnsEmpty() {
        repo.save(new Car(1L, "Toyota", "Camry", "A1", 2020));
        assertTrue(repo.findByClientId(999L).isEmpty());
    }

    @Test
    void findByClientId_nullClientId_doesNotCrash() {
        repo.save(new Car(1L, "Toyota", "Camry", "A1", 2020));
        assertTrue(repo.findByClientId(null).isEmpty());
    }
}