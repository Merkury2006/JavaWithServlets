package vsu.cs.ui;

import vsu.cs.facade.AutoServiceFacade;
import vsu.cs.ui.controllers.CarController;
import vsu.cs.ui.controllers.ClientController;
import vsu.cs.ui.controllers.WorkOrderController;

public class ConsoleApp {
    private final ConsoleIO io;
    private final ClientController clientController;
    private final CarController carController;
    private final WorkOrderController orderController;

    public ConsoleApp(AutoServiceFacade facade) {
        this.io = new ConsoleIO();
        this.clientController = new ClientController(facade, io);
        this.carController = new CarController(facade, io);
        this.orderController = new WorkOrderController(facade, io);
    }

    public void start() {
        while (true) {
            System.out.println("""
                    === Auto Service ===
                    1. Clients
                    2. Cars
                    3. Work orders
                    0. Exit
                    """);
            String choice = io.readLine("> ").trim();
            switch (choice) {
                case "1" -> clientController.run();
                case "2" -> carController.run();
                case "3" -> orderController.run();
                case "0" -> {
                    System.out.println("Bye.");
                    return;
                }
                default -> System.out.println("Unknown command.");
            }
        }
    }
}