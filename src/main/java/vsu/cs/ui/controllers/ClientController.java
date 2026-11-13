package vsu.cs.ui.controllers;

import vsu.cs.domain.Client;
import vsu.cs.dto.console.DtoMapper;
import vsu.cs.dto.console.response.ClientResponse;
import vsu.cs.facade.ClientFacade;
import vsu.cs.ui.ConsoleIO;

import java.util.List;

public class ClientController {
    private final ClientFacade facade;
    private final ConsoleIO io;

    public ClientController(ClientFacade facade, ConsoleIO io) {
        this.facade = facade;
        this.io = io;
    }

    public void run() {
        while (true) {
            System.out.println("""
                    
                    --- Clients ---
                    1. List all
                    2. Create
                    3. Update
                    4. Delete (cascade)
                    0. Back
                    """);
            String choice = io.readLine("> ").trim();
            switch (choice) {
                case "1" -> io.safe(this::list);
                case "2" -> io.safe(this::create);
                case "3" -> io.safe(this::update);
                case "4" -> io.safe(this::delete);
                case "0" -> { return; }
                default -> System.out.println("Unknown command.");
            }
        }
    }

    private void list() {
        List<ClientResponse> clients = facade.getAll().stream().map(DtoMapper::toResponse).toList();
        if (clients.isEmpty()) {
            System.out.println("(no clients)");
            return;
        }
        clients.forEach(System.out::println);
    }

    private void create() {
        String fullName = io.readLine("Full name: ");
        String phone = io.readLine("Phone: ");
        String email = io.readLine("Email: ");
        Client created = facade.create(fullName, phone, email);
        System.out.println("Created: " + DtoMapper.toResponse(created));
    }

    private void update() {
        Long id = io.readLong("Client id: ");
        String fullName = io.readLine("Full name: ");
        String phone = io.readLine("Phone: ");
        String email = io.readLine("Email: ");
        Client updated = facade.update(id, fullName, phone, email);
        System.out.println("Updated: " + DtoMapper.toResponse(updated));
    }

    private void delete() {
        Long id = io.readLong("Client id: ");
        facade.delete(id);
        System.out.println("Deleted client " + id + " (with cars, orders, items).");
    }
}