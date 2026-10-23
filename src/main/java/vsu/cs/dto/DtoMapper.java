package vsu.cs.dto;

import vsu.cs.domain.*;
import vsu.cs.dto.response.*;

public final class DtoMapper {

    private DtoMapper() {}

    public static ClientResponse toResponse(Client c) {
        return new ClientResponse(c.getId(), c.getFullName(), c.getPhone(), c.getEmail());
    }

    public static CarResponse toResponse(Car c) {
        return new CarResponse(c.getId(), c.getClientId(), c.getBrand(), c.getModel(),
                c.getPlateNumber(), c.getYear());
    }

    public static WorkOrderResponse toResponse(WorkOrder o) {
        return new WorkOrderResponse(o.getId(), o.getCarId(), o.getStatus(),
                o.getCreatedAt(), o.getTotalPrice());
    }

    public static WorkOrderItemResponse toResponse(WorkOrderItem i) {
        return new WorkOrderItemResponse(i.getId(), i.getWorkOrderId(), i.getDescription(),
                i.getPrice(), i.getQuantity(), i.getSubtotal());
    }
}