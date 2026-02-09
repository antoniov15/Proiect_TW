package com.financeassistant.transaction.entity;

import com.financeassistant.transaction.dto.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EntityCoverageTest {

    @Test
    void testTransactionEntity() {
        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(BigDecimal.TEN);
        t.setDescription("Test");
        t.setDate(LocalDate.now());
        t.setUserId(100L);
        t.setType(TransactionType.EXPENSE);

        Category c = new Category();
        c.setId(2L);
        c.setName("Food");
        t.setCategory(c);

        assertEquals(1L, t.getId());
        assertEquals(BigDecimal.TEN, t.getAmount());
        assertNotNull(t.getCategory());
        assertNotNull(t.toString());
        assertEquals(t, t);
        assertNotEquals(t, new Transaction());
        assertEquals(t.hashCode(), t.hashCode());
    }

    @Test
    void testDTOs() {
        SmartTransactionDTO smart = new SmartTransactionDTO();
        smart.setAmount(50.0);
        smart.setDescription("Uber");
        assertEquals(50.0, smart.getAmount());
        assertNotNull(smart.toString());

        AccountResponseDTO account = new AccountResponseDTO();
        account.setId(1L);
        account.setEmail("test@test.com");
        account.setName("Test User");
        assertEquals("test@test.com", account.getEmail());
    }
}