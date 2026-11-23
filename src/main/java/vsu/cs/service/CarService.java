package vsu.cs.service;

import vsu.cs.domain.Car;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.repository.CarRepository;
import vsu.cs.repository.ClientRepository;
import vsu.cs.repository.WorkOrderItemRepository;
import vsu.cs.repository.WorkOrderRepository;
import vsu.cs.utils.Validator;

import java.util.List;

public class CarService {
    private final CarRepository carRepository;
    private final ClientRepository clientRepository;
    private final WorkOrderRepository orderRepository;
    private final WorkOrderItemRepository itemRepository;

    public CarService(CarRepository carRepo, ClientRepository clientRepo, WorkOrderRepository orderRepo, WorkOrderItemRepository itemRepo) {
        this.carRepository = carRepo;
        this.clientRepository = clientRepo;
        this.orderRepository = orderRepo;
        this.itemRepository = itemRepo;
    }

    public Car createCar(Long clientId, String brand, String model, String plateNumber, int year) {
        if (!clientRepository.existsById(clientId)) {
            throw new EntityNotFoundException("Client not found: " + clientId);
        }
        Validator.requireNotBlank(brand, "Car brand");
        Validator.requireNotBlank(model, "Car model");
        Validator.requireYearInRange(year, "Car year");

        return carRepository.save(new Car(clientId, brand, model, plateNumber, year));
    }

    public List<Car> getCarsByClient(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new EntityNotFoundException("Client not found: " + clientId);
        }
        return carRepository.findByClientId(clientId);
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Car getCar(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Car not found: " + id)
        );
    }

    public void deleteCar(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Car not found: " + id);
        }
        for (WorkOrder order : orderRepository.findByCarId(id)) {
            itemRepository.deleteByWorkOrderId(order.getId());
            orderRepository.deleteById(order.getId());
        }
        carRepository.deleteById(id);
    }
}
