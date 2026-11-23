package vsu.cs.facade;

import vsu.cs.domain.Client;
import vsu.cs.service.ClientService;

import java.util.List;

public final class ClientFacade {
    private final ClientService clientService;

    public ClientFacade(ClientService clientService) {
        this.clientService = clientService;
    }

    public Client create(String fullName, String phone, String email) {
        return clientService.createClient(fullName, phone, email);
    }

    public Client update(Long id, String fullName, String phone, String email) {
        return clientService.updateClient(id, fullName, phone, email);
    }

    public List<Client> getAll() {
        return clientService.getAllClients();
    }

    public Client get(Long id) {
        return clientService.getClient(id);
    }

    public void delete(Long id) {
        clientService.deleteClient(id);
    }
}