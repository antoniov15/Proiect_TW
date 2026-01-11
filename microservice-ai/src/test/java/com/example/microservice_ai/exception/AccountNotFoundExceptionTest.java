package com.example.microservice_ai.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AccountNotFoundException.
 */
@DisplayName("AccountNotFoundException Unit Tests")
class AccountNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void testExceptionWithMessage() {
        String message = "Chat not found: 123";
        AccountNotFoundException exception = new AccountNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void testExceptionType() {
        AccountNotFoundException exception = new AccountNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }
}
