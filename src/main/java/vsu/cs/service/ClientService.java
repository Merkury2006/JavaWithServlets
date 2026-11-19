package vsu.cs.service;

import vsu.cs.domain.Client;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.repository.ClientRepository;
import vsu.cs.utils.Validator;

import java.util.List;

public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client createClient(String fullName, String phone, String email) {
        Validator.requireNotBlank(fullName, "Client full name");
        return clientRepository.save(new Client(fullName, phone, email));
    }

    public Client updateClient(Long id, String fullName, String phone, String email) {
        Client client = clientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Client not found: " + id)
        );

        Validator.requireNotBlank(fullName, "Client full name");

        client.setFullName(fullName);
        client.setPhone(phone);
        client.setEmail(email);

        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClient(Long id) {
        return clientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Client not found: " + id)
        );
    }

    public void deleteClient(Long id) {
        if (!clientRepository.deleteById(id)) {
            throw new EntityNotFoundException("Client not found: " + id);
        }
    }
}
