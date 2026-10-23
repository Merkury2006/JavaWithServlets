package vsu.cs.ui.controllers;

import vsu.cs.dto.request.CreateClientRequest;
import vsu.cs.dto.request.UpdateClientRequest;
import vsu.cs.dto.response.ClientResponse;
import vsu.cs.facade.AutoServiceFacade;
import vsu.cs.ui.ConsoleIO;

import java.util.List;

public class ClientController {
    private final AutoServiceFacade facade;
    private final ConsoleIO io;

    public ClientController(AutoServiceFacade facade, ConsoleIO io) {
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
        List<ClientResponse> clients = facade.getAllClients();
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
        ClientResponse response = facade.createClient(new CreateClientRequest(fullName, phone, email));
        System.out.println("Created: " + response);
    }

    private void update() {
        Long id = io.readLong("Client id: ");
        String fullName = io.readLine("Full name: ");
        String phone = io.readLine("Phone: ");
        String email = io.readLine("Email: ");
        ClientResponse response = facade.updateClient(new UpdateClientRequest(id, fullName, phone, email));
        System.out.println("Updated: " + response);
    }

    private void delete() {
        Long id = io.readLong("Client id: ");
        facade.deleteClient(id);
        System.out.println("Deleted client " + id + " (with cars, orders, items).");
    }
}