package com.financeassistant.transaction;

import com.financeassistant.transaction.dto.*;
import com.financeassistant.transaction.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PojoCoverageTest {

    @Test
    void testTransactionEntity() {
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(BigDecimal.TEN);
        t1.setDescription("Test");
        t1.setDate(LocalDate.now());
        t1.setUserId(100L);
        t1.setType(TransactionType.EXPENSE);

        Category c = new Category();
        c.setId(2L);
        t1.setCategory(c);

        assertEquals(1L, t1.getId());
        assertEquals(BigDecimal.TEN, t1.getAmount());
        assertEquals("Test", t1.getDescription());
        assertNotNull(t1.getDate());
        assertEquals(100L, t1.getUserId());
        assertEquals(TransactionType.EXPENSE, t1.getType());
        assertNotNull(t1.getCategory());

        // Test Lombok generated methods
        Transaction t2 = new Transaction();
        t2.setId(1L); // Same ID usually implies equality in Entities depending on implementation
        // Forcing equals check if implemented, otherwise mostly checks object identity or all fields for @Data

        assertNotNull(t1.toString());
    }

    @Test
    void testCategoryEntity() {
        Category c = new Category();
        c.setId(1L);
        c.setName("Food");
        c.setType(TransactionType.EXPENSE);

        assertEquals(1L, c.getId());
        assertEquals("Food", c.getName());
        assertEquals(TransactionType.EXPENSE, c.getType());
        assertNotNull(c.toString());
    }

    @Test
    void testSmartTransactionDTO() {
        SmartTransactionDTO dto = new SmartTransactionDTO();
        dto.setAmount(50.0);
        dto.setDescription("Uber");

        assertEquals(50.0, dto.getAmount());
        assertEquals("Uber", dto.getDescription());

        SmartTransactionDTO dto2 = new SmartTransactionDTO();
        dto2.setAmount(50.0);
        dto2.setDescription("Uber");

        assertEquals(dto, dto2); // Tests equals
        assertEquals(dto.hashCode(), dto2.hashCode()); // Tests hashCode
        assertNotNull(dto.toString()); // Tests toString
    }

    @Test
    void testUpdateTransactionDTO() {
        UpdateTransactionDTO dto = new UpdateTransactionDTO();
        dto.setAmount(BigDecimal.ONE);
        dto.setDescription("Update");
        dto.setDate(LocalDate.now());
        dto.setCategoryId(5L);

        assertEquals(BigDecimal.ONE, dto.getAmount());
        assertEquals("Update", dto.getDescription());
        assertEquals(5L, dto.getCategoryId());
        assertNotNull(dto.toString());
    }

    @Test
    void testTransactionViewDTO() {
        TransactionViewDTO dto = new TransactionViewDTO();
        dto.setId(10L);
        dto.setAmount(BigDecimal.ZERO);
        dto.setType(TransactionType.INCOME);

        assertEquals(10L, dto.getId());
        assertEquals(BigDecimal.ZERO, dto.getAmount());
        assertEquals(TransactionType.INCOME, dto.getType());
        assertNotNull(dto.toString());
    }

    @Test
    void testAccountResponseDTO() {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(1L);
        dto.setEmail("a@b.c");
        dto.setName("User");

        assertEquals(1L, dto.getId());
        assertEquals("a@b.c", dto.getEmail());
        assertEquals("User", dto.getName());

        AccountResponseDTO dto2 = new AccountResponseDTO();
        dto2.setId(1L);
        dto2.setEmail("a@b.c");
        dto2.setName("User");

        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());
    }
}