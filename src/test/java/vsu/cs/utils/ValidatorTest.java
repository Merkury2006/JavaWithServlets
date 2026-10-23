package vsu.cs.utils;

import org.junit.jupiter.api.Test;
import vsu.cs.domain.OrderStatus;
import vsu.cs.exception.InvalidStateException;
import vsu.cs.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    void requireNotBlank_throwsOnNull() {
        assertThrows(ValidationException.class,
                () -> Validator.requireNotBlank(null, "Name"));
    }

    @Test
    void requireNotBlank_throwsOnBlank() {
        assertThrows(ValidationException.class,
                () -> Validator.requireNotBlank("   ", "Name"));
    }

    @Test
    void requireNotBlank_ok() {
        assertDoesNotThrow(() -> Validator.requireNotBlank("Иван", "Name"));
    }

    @Test
    void requirePositive_double_throwsOnNegative() {
        assertThrows(ValidationException.class,
                () -> Validator.requirePositive(-1.0, "Price"));
    }

    @Test
    void requirePositive_double_okOnZero() {
        assertDoesNotThrow(() -> Validator.requirePositive(0.0, "Price"));
    }

    @Test
    void requirePositive_int_throwsOnZero() {
        assertThrows(ValidationException.class,
                () -> Validator.requirePositive(0, "Quantity"));
    }

    @Test
    void requireYearInRange_throwsOnTooOld() {
        assertThrows(ValidationException.class,
                () -> Validator.requireYearInRange(1800, "Year"));
    }

    @Test
    void requireYearInRange_ok() {
        assertDoesNotThrow(() -> Validator.requireYearInRange(2020, "Year"));
    }

    @Test
    void requireAllowedTransition_allowsNewToInProgress() {
        assertDoesNotThrow(() ->
                Validator.requireAllowedTransitionChangeStatus(OrderStatus.NEW, OrderStatus.IN_PROGRESS));
    }

    @Test
    void requireAllowedTransition_forbidsNewToDone() {
        assertThrows(InvalidStateException.class, () ->
                Validator.requireAllowedTransitionChangeStatus(OrderStatus.NEW, OrderStatus.DONE));
    }

    @Test
    void requireAllowedTransition_forbidsFromDone() {
        assertThrows(InvalidStateException.class, () ->
                Validator.requireAllowedTransitionChangeStatus(OrderStatus.DONE, OrderStatus.IN_PROGRESS));
    }
}