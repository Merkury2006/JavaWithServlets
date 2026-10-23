package vsu.cs.utils;

import vsu.cs.domain.OrderStatus;
import vsu.cs.domain.WorkOrder;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.exception.ValidationException;

import java.time.LocalDateTime;

import static vsu.cs.domain.Constraints.FIRST_YEAR_OF_CARS;
import static vsu.cs.domain.Constraints.MAX_YEAR_AHEAD;

public final class Validator {

    private Validator() {}

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
    }

    public static void requirePositive(double value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " must be >= 0");
        }
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be > 0");
        }
    }

    public static void requireYearInRange(int year, String fieldName) {
        int max = LocalDateTime.now().getYear() + MAX_YEAR_AHEAD;
        if (year < FIRST_YEAR_OF_CARS || year > max) {
            throw new ValidationException("Invalid " + fieldName + ": " + year + " (must be " + FIRST_YEAR_OF_CARS + ".." + max + ")");
        }
    }

    public static void requireAllowedTransitionChangeStatus(OrderStatus from, OrderStatus to) {
        boolean allowed =  switch (from) {
            case NEW -> to == OrderStatus.IN_PROGRESS || to == OrderStatus.CANCELLED;
            case IN_PROGRESS -> to == OrderStatus.DONE || to == OrderStatus.CANCELLED;
            case DONE, CANCELLED -> false;
        };

        if (!allowed) {
            throw new InvalidStateException("Illegal status transition: " + from + " -> " + to);
        }
    }

    public static void requireWorkOrderEditable(WorkOrder order) {
        if (order.getStatus() == OrderStatus.DONE || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidStateException("Cannot modify work order in status " + order.getStatus());
        }
    }
}