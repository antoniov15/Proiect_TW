package com.financeassistant.transaction.dto;

import com.financeassistant.transaction.entity.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CreateTransactionDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_ValidDTO_NoViolations() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                1L,
                new BigDecimal("100.00"),
                LocalDate.now(),
                "Valid Description",
                TransactionType.EXPENSE,
                1L
        );

        Set<ConstraintViolation<CreateTransactionDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(), "DTO should be valid");
    }

    @Test
    void validate_NullFields_ReturnsViolations() {
        CreateTransactionDTO dto = new CreateTransactionDTO();

        Set<ConstraintViolation<CreateTransactionDTO>> violations = validator.validate(dto);

        assertEquals(5, violations.size(), "There should be 5 violations for null fields");
    }

    @Test
    void validate_NegativeAmount_ReturnsViolation() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                1L,
                new BigDecimal("-50.00"),
                LocalDate.now(),
                "Negative Amount Test",
                TransactionType.EXPENSE,
                1L
        );

        Set<ConstraintViolation<CreateTransactionDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(), "There should be 1 violation for negative amount");
        assertEquals("Amount must be positive", violations.iterator().next().getMessage());
    }

    @Test
    void validate_FutureDate_ReturnsViolation() {
        CreateTransactionDTO dto = new CreateTransactionDTO(
                1L,
                new BigDecimal("100.00"),
                LocalDate.now().plusDays(1),
                "Future Date Test",
                TransactionType.INCOME,
                1L
        );

        Set<ConstraintViolation<CreateTransactionDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size(), "There should be 1 violation for future date");
        assertTrue(violations.iterator().next().getMessage().contains("future"));
    }
}
