package com.financeassistant.transaction.mapper;

import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.Transaction;
import com.financeassistant.transaction.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new TransactionMapper();
    }

    @Test
    void toViewDTO_ValidEntity_ReturnsDTO() {

        Category category = new Category();
        category.setId(5L);
        category.setName("Groceries");

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("150.00"));
        transaction.setDate(LocalDate.of(2025, 10, 10));
        transaction.setDescription("Electricity Bill");
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(category);

        TransactionViewDTO result = mapper.toViewDTO(transaction);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals("Groceries", result.getCategoryName());
        assertEquals(TransactionType.EXPENSE, result.getType());
    }

    @Test
    void toViewDTO_NullCategory_ReturnsDTOWithNullCategoryName() {

        Transaction transaction = new Transaction();
        transaction.setId(2L);
        transaction.setAmount(new BigDecimal("200.00"));
        transaction.setCategory(null);

        TransactionViewDTO result = mapper.toViewDTO(transaction);

        assertNotNull(result);
        assertNull(result.getCategoryName());
        assertEquals(new BigDecimal("200.00"), result.getAmount());
    }

    @Test
    void toViewDTO_ListOfEntities_ReturnsListOfDTOs() {

        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(new BigDecimal("150.00"));

        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setAmount(new BigDecimal("200.00"));

        List<Transaction> transactions = List.of(t1, t2);

        List<TransactionViewDTO> results = mapper.toViewDTOs(transactions);

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
    }
}
