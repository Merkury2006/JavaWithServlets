package vsu.cs;

import vsu.cs.ui.ConsoleApp;

public class Main {
    public static void main(String[] args) {
        AppContainer container = AppFactory.createInMemory();
        new ConsoleApp(container.clientFacade, container.carFacade, container.workOrderFacade).start();
    }
}