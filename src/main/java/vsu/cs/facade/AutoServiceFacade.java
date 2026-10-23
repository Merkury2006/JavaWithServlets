package vsu.cs.facade;

import vsu.cs.domain.*;
import vsu.cs.dto.DtoMapper;
import vsu.cs.dto.request.*;
import vsu.cs.dto.response.CarResponse;
import vsu.cs.dto.response.ClientResponse;
import vsu.cs.dto.response.WorkOrderItemResponse;
import vsu.cs.dto.response.WorkOrderResponse;
import vsu.cs.service.CarService;
import vsu.cs.service.ClientService;
import vsu.cs.service.WorkOrderItemService;
import vsu.cs.service.WorkOrderService;

import java.util.List;

public final class AutoServiceFacade {
    private final ClientService clientService;
    private final CarService carService;
    private final WorkOrderService orderService;
    private final WorkOrderItemService itemService;

    public AutoServiceFacade(ClientService clientService, CarService carService, WorkOrderService orderService, WorkOrderItemService itemService) {
        this.clientService = clientService;
        this.carService = carService;
        this.orderService = orderService;
        this.itemService = itemService;
    }

    public ClientResponse createClient(CreateClientRequest request) {
        return DtoMapper.toResponse(clientService.createClient(request.fullName(), request.phone(), request.email()));
    }
    public ClientResponse updateClient(UpdateClientRequest request) {
        return DtoMapper.toResponse(clientService.updateClient(request.id(), request.fullName(), request.phone(), request.email()));
    }
    public List<ClientResponse> getAllClients() {
        return clientService.getAllClients().stream().map(DtoMapper::toResponse).toList();
    }
    public ClientResponse getClient(Long id) {
        return DtoMapper.toResponse(clientService.getClient(id));
    }
    public void deleteClient(Long id) { clientService.deleteClient(id); }


    public CarResponse createCar(CreateCarRequest request) {
        return DtoMapper.toResponse(carService.createCar(request.clientId(), request.brand(), request.model(), request.plateNumber(), request.year()));
    }
    public List<CarResponse> getCarsByClient(Long clientId) {
        return carService.getCarsByClient(clientId).stream().map(DtoMapper::toResponse).toList();
    }
    public List<CarResponse> getAllCars() {
        return carService.getAllCars().stream().map(DtoMapper::toResponse).toList();
    }
    public CarResponse getCar(Long id) {
        return DtoMapper.toResponse(carService.getCar(id));
    }
    public void deleteCar(Long id) { carService.deleteCar(id); }


    public WorkOrderResponse createWorkOrder(CreateWorkOrderRequest request) {
        return DtoMapper.toResponse(orderService.createWorkOrder(request.carId()));
    }
    public List<WorkOrderResponse> getOrdersByCar(Long carId) {
        return orderService.getOrdersByCar(carId).stream().map(DtoMapper::toResponse).toList();
    }
    public List<WorkOrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderService.getOrdersByStatus(status).stream().map(DtoMapper::toResponse).toList();
    }
    public List<WorkOrderResponse> getAllWorkOrders() {
        return orderService.getAllWorkOrders().stream().map(DtoMapper::toResponse).toList();
    }
    public WorkOrderResponse getWorkOrder(Long id) {
        return DtoMapper.toResponse(orderService.getWorkOrder(id));
    }
    public void deleteWorkOrder(Long id) { orderService.deleteWorkOrder(id); }
    public WorkOrderResponse changeStatus(ChangeStatusRequest request) {
        return DtoMapper.toResponse(orderService.changeStatus(request.workOrderId(), request.newStatus()));
    }

    public WorkOrderItemResponse addItem(AddItemRequest request) {
        return DtoMapper.toResponse(itemService.addItem(request.workOrderId(), request.description(), request.price(), request.quantity()));
    }
    public void removeItem(Long itemId) { itemService.removeItem(itemId); }
    public List<WorkOrderItemResponse> getItems(Long workOrderId) {
        return itemService.getItems(workOrderId).stream().map(DtoMapper::toResponse).toList();
    }
}

