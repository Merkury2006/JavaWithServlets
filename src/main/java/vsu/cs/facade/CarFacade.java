package vsu.cs.facade;

import vsu.cs.domain.Car;
import vsu.cs.service.CarService;

import java.util.List;

public final class CarFacade {
    private final CarService carService;

    public CarFacade(CarService carService) {
        this.carService = carService;
    }

    public Car create(Long clientId, String brand, String model, String plateNumber, int year) {
        return carService.createCar(clientId, brand, model, plateNumber, year);
    }

    public List<Car> getByClient(Long clientId) {
        return carService.getCarsByClient(clientId);
    }

    public List<Car> getAll() {
        return carService.getAllCars();
    }

    public Car get(Long id) {
        return carService.getCar(id);
    }

    public void delete(Long id) {
        carService.deleteCar(id);
    }
}