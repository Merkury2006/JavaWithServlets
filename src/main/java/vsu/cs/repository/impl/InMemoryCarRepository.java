package vsu.cs.repository.impl;

import vsu.cs.domain.Car;
import vsu.cs.repository.CarRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCarRepository extends InMemoryRepository<Car, Long> implements CarRepository {
    public InMemoryCarRepository() {
        super(new AtomicLong(0)::incrementAndGet);
    }

    @Override
    public List<Car> findByClientId(Long id) {
        return this.storage.values().stream().filter(car -> Objects.equals(car.getClientId(), id)).toList();
    }
}
