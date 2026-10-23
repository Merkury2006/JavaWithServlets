package vsu.cs;

import vsu.cs.facade.AutoServiceFacade;
import vsu.cs.ui.ConsoleApp;

public class Main {
    public static void main(String[] args) {
        AutoServiceFacade facade = AppFactory.createInMemoryAutoServiceFacade();
        new ConsoleApp(facade).start();
    }
}