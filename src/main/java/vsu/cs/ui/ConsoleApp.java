package vsu.cs.ui;

import vsu.cs.facade.CarFacade;
import vsu.cs.facade.ClientFacade;
import vsu.cs.facade.WorkOrderFacade;
import vsu.cs.ui.controllers.CarController;
import vsu.cs.ui.controllers.ClientController;
import vsu.cs.ui.controllers.WorkOrderController;

public class ConsoleApp {
    private final ConsoleIO io;
    private final ClientController clientController;
    private final CarController carController;
    private final WorkOrderController orderController;

    public ConsoleApp(ClientFacade clientFacade, CarFacade carFacade, WorkOrderFacade workOrderFacade) {
        this.io = new ConsoleIO();
        this.clientController = new ClientController(clientFacade, io);
        this.carController = new CarController(carFacade, io);
        this.orderController = new WorkOrderController(workOrderFacade, io);
    }

    public void start() {
        new Menu(io, "Auto Service")
                .add("clients","Clients", clientController::run)
                .add("cars","Cars", carController::run)
                .add("orders","Work orders", orderController::run)
                .exit("exit","Exit")
                .run();
        System.out.println("Bye.");
    }
}