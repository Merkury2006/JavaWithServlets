package vsu.cs.ui.controllers;

import vsu.cs.domain.Car;
import vsu.cs.dto.console.DtoMapper;
import vsu.cs.dto.console.response.CarResponse;
import vsu.cs.facade.CarFacade;
import vsu.cs.ui.ConsoleIO;
import vsu.cs.ui.Menu;

import java.util.List;

public class CarController {
    private final CarFacade facade;
    private final ConsoleIO io;

    public CarController(CarFacade facade, ConsoleIO io) {
        this.facade = facade;
        this.io = io;
    }

    public void run() {
        new Menu(io, "Cars")
                .add("all","List all", this::listAll)
                .add("by-client","List by client", this::listByClient)
                .add("create","Create", this::create)
                .add("delete","Delete (cascade)", this::delete)
                .run();
    }

    private void listAll() {
        List<CarResponse> cars = facade.getAll().stream().map(DtoMapper::toResponse).toList();
        if (cars.isEmpty()) {
            System.out.println("(no cars)");
            return;
        }
        cars.forEach(System.out::println);
    }

    private void listByClient() {
        Long clientId = io.readLong("Client id: ");
        List<CarResponse> cars = facade.getByClient(clientId).stream().map(DtoMapper::toResponse).toList();
        if (cars.isEmpty()) {
            System.out.println("(no cars for client " + clientId + ")");
            return;
        }
        cars.forEach(System.out::println);
    }

    private void create() {
        Long clientId = io.readLong("Client id: ");
        String brand = io.readLine("Brand: ");
        String model = io.readLine("Model: ");
        String plate = io.readLine("Plate number: ");
        int year = io.readInt("Year: ");
        Car created = facade.create(clientId, brand, model, plate, year);
        System.out.println("Created: " + DtoMapper.toResponse(created));
    }

    private void delete() {
        Long id = io.readLong("Car id: ");
        facade.delete(id);
        System.out.println("Deleted car " + id + " (with orders, items).");
    }
}