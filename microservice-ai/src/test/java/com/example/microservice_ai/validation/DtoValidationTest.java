package com.example.microservice_ai.validation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.microservice_ai.dto.ChatCreateDTO;
import com.example.microservice_ai.dto.MessageCreateDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Validation tests for DTOs.
 */
@DisplayName("DTO Validation Tests")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("ChatCreateDTO should fail validation with blank title")
    void testChatCreateDTOBlankTitle() {
        ChatCreateDTO dto = new ChatCreateDTO("", null);
        Set<ConstraintViolation<ChatCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("ChatCreateDTO should pass validation with valid title")
    void testChatCreateDTOValidTitle() {
        ChatCreateDTO dto = new ChatCreateDTO("Valid Title", null);
        Set<ConstraintViolation<ChatCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("MessageCreateDTO should fail validation with blank content")
    void testMessageCreateDTOBlankContent() {
        MessageCreateDTO dto = new MessageCreateDTO("USER", "");
        Set<ConstraintViolation<MessageCreateDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }
}
