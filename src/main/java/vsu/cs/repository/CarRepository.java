package vsu.cs.repository;

import vsu.cs.domain.Car;

import java.util.List;

public interface CarRepository extends Repository<Car, Long> {
    List<Car> findByClientId(Long id);
}
