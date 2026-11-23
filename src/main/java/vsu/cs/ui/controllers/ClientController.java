package vsu.cs.ui.controllers;

import vsu.cs.domain.Client;
import vsu.cs.dto.console.DtoMapper;
import vsu.cs.dto.console.response.ClientResponse;
import vsu.cs.facade.ClientFacade;
import vsu.cs.ui.ConsoleIO;
import vsu.cs.ui.Menu;

import java.util.List;

public class ClientController {
    private final ClientFacade facade;
    private final ConsoleIO io;

    public ClientController(ClientFacade facade, ConsoleIO io) {
        this.facade = facade;
        this.io = io;
    }

    public void run() {
        new Menu(io, "Clients")
                .add("list","List all", this::list)
                .add("create","Create", this::create)
                .add("update","Update", this::update)
                .add("delete","Delete (cascade)", this::delete)
                .run();
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