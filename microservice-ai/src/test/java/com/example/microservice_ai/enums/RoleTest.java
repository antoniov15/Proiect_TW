package com.example.microservice_ai.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Role enum.
 */
@DisplayName("Role Enum Unit Tests")
class RoleTest {

    @Test
    @DisplayName("Should have exactly three roles")
    void testRoleCount() {
        assertEquals(3, Role.values().length);
    }

    @Test
    @DisplayName("USER role should have correct code")
    void testUserRoleCode() {
        assertEquals("user", Role.USER.getCode());
    }

    @Test
    @DisplayName("Should get role from code")
    void testFromCode() {
        assertEquals(Role.USER, Role.fromCode("user"));
        assertEquals(Role.ASSISTANT, Role.fromCode("assistant"));
        assertEquals(Role.CONTEXT, Role.fromCode("system"));
    }

    @Test
    @DisplayName("Should return null for invalid code")
    void testFromCodeInvalid() {
        assertNull(Role.fromCode("invalid"));
    }
}
