package vsu.cs.ui.controllers;

import vsu.cs.domain.Car;
import vsu.cs.dto.console.DtoMapper;
import vsu.cs.dto.console.response.CarResponse;
import vsu.cs.facade.CarFacade;
import vsu.cs.ui.ConsoleIO;

import java.util.List;

public class CarController {
    private final CarFacade facade;
    private final ConsoleIO io;

    public CarController(CarFacade facade, ConsoleIO io) {
        this.facade = facade;
        this.io = io;
    }

    public void run() {
        while (true) {
            System.out.println("""
                    
                    --- Cars ---
                    1. List all
                    2. List by client
                    3. Create
                    4. Delete (cascade)
                    0. Back
                    """);
            String choice = io.readLine("> ").trim();
            switch (choice) {
                case "1" -> io.safe(this::listAll);
                case "2" -> io.safe(this::listByClient);
                case "3" -> io.safe(this::create);
                case "4" -> io.safe(this::delete);
                case "0" -> { return; }
                default -> System.out.println("Unknown command.");
            }
        }
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