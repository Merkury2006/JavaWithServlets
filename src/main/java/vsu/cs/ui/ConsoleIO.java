package vsu.cs.ui;

import vsu.cs.domain.OrderStatus;
import vsu.cs.exception.EntityNotFoundException;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.exception.ValidationException;

import java.util.Scanner;

public class ConsoleIO {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public Long readLong(String prompt) {
        while (true) {
            String line = readLine(prompt).trim();
            try {
                return Long.parseLong(line);
            } catch (NumberFormatException e) {
                System.out.println("Not a number: " + line);
            }
        }
    }

    public int readInt(String prompt) {
        while (true) {
            String line = readLine(prompt).trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Not a number: " + line);
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String line = readLine(prompt).trim().replace(',', '.');
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Not a number: " + line);
            }
        }
    }

    public OrderStatus readStatus() {
        while (true) {
            String line = readLine("Status (NEW/IN_PROGRESS/DONE/CANCELLED): ").trim().toUpperCase();
            try {
                return OrderStatus.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown status: " + line);
            }
        }
    }

    public void safe(Runnable action) {
        try {
            action.run();
        } catch (EntityNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("[VALIDATION] " + e.getMessage());
        } catch (InvalidStateException e) {
            System.out.println("[INVALID STATE] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}
