package vsu.cs.service;

import vsu.cs.domain.Car;
import vsu.cs.domain.Client;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.repository.CarRepository;
import vsu.cs.repository.ClientRepository;
import vsu.cs.repository.WorkOrderItemRepository;
import vsu.cs.repository.WorkOrderRepository;
import vsu.cs.utils.Validator;

import java.util.List;

public class ClientService {
    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    private final WorkOrderRepository orderRepository;
    private final WorkOrderItemRepository itemRepository;

    public ClientService(ClientRepository clientRepository, CarRepository carRepository, WorkOrderRepository orderRepository, WorkOrderItemRepository itemRepository) {
        this.clientRepository = clientRepository;
        this.carRepository = carRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
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
        if (!clientRepository.existsById(id)) {
            throw new EntityNotFoundException("Client not found: " + id);
        }

        List<Car> cars = carRepository.findByClientId(id);
        for (Car car : cars) {
            this.deleteCarWithOrders(car.getId());
        }
        clientRepository.deleteById(id);
    }

    private void deleteCarWithOrders(Long carId) {
        for (WorkOrder order : orderRepository.findByCarId(carId)) {
            itemRepository.deleteByWorkOrderId(order.getId());
            orderRepository.deleteById(order.getId());
        }
        carRepository.deleteById(carId);
    }
}
