package com.financeassistant.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.exception.ResourceNotFoundException;
import com.financeassistant.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTransaction_ValidInput_ReturnsCreated() throws Exception {

        CreateTransactionDTO inputDto = new CreateTransactionDTO(
            1L, new BigDecimal("100.0"), LocalDate.now(), "Test Transaction", TransactionType.EXPENSE, 1L
        );

        TransactionViewDTO outputDto = new TransactionViewDTO(
                1L, new BigDecimal("100.0"), LocalDate.now(), "Test Transaction", TransactionType.EXPENSE, "Category Name"
        );

        when(transactionService.createTransaction(any(CreateTransactionDTO.class)))
            .thenReturn(outputDto);

        mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(outputDto.getId()))
                .andExpect(jsonPath("$.amount").value(outputDto.getAmount()));
    }

    @Test
    void createTransaction_NegativeAmount_ReturnsBadRequest() throws Exception {

        CreateTransactionDTO invalidDto = new CreateTransactionDTO(
            1L, new BigDecimal("-100.00"), LocalDate.now(), "Test Transaction", TransactionType.EXPENSE, 1L
        );

        mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void createTransaction_FutureDate_ReturnsBadRequest() throws Exception {

        CreateTransactionDTO invalidDto = new CreateTransactionDTO(
                1L, new BigDecimal("100.00"), LocalDate.now().plusDays(1), "Test Transaction", TransactionType.EXPENSE, 1L
        );

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    void getTransactionById_ExistingId_ReturnsOk() throws Exception {

        TransactionViewDTO dto = new TransactionViewDTO();
        dto.setId(1L);
        dto.setAmount(new BigDecimal("500.00"));

        when(transactionService.getTransactionById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void getTransactionById_NonExistingId_ReturnsNotFound() throws Exception {

        when(transactionService.getTransactionById(99L))
                .thenThrow(new ResourceNotFoundException("Not Found"));

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getTransactionsByUserId_ValidRequest_ReturnsOk() throws Exception {

        TransactionViewDTO t1 = new TransactionViewDTO();
        t1.setId(1L);
        TransactionViewDTO t2 = new TransactionViewDTO();
        t2.setId(2L);

        when(transactionService.getTransactionsByUserId(eq(1L), any(), any(), any()))
                .thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/transactions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void updateTransaction_ValidInput_ReturnsOk() throws Exception {

        Long id = 1L;
        UpdateTransactionDTO inputDto = new UpdateTransactionDTO(
            new BigDecimal("200.00"), "Updated Transaction", LocalDate.now(), 2L
        );

        TransactionViewDTO responseDto = new TransactionViewDTO(
                id, new BigDecimal("200.00"), LocalDate.now(), "Updated Transaction", TransactionType.EXPENSE, "Updated Category"
        );

        when(transactionService.updateTransaction(eq(id), any(UpdateTransactionDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/transactions/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.description").value("Updated Transaction"));
    }

    @Test
    void updateTransaction_NonExistingId_ReturnsNotFound() throws Exception {

        Long id = 99L;
        UpdateTransactionDTO inputDto = new UpdateTransactionDTO(
            new BigDecimal("200.00"), "Updated Transaction", LocalDate.now(), 2L
        );

        when(transactionService.updateTransaction(eq(id), any(UpdateTransactionDTO.class)))
                .thenThrow(new ResourceNotFoundException("Not Found"));

        mockMvc.perform(put("/api/transactions/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void deleteTransactionById_ExistingId_ReturnsNoContent() throws Exception {

        Long id = 1L;

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTransactionById_NonExistingId_ReturnsNotFound() throws Exception {

        Long id = 99L;

        doThrow(new ResourceNotFoundException("Not Found"))
                .when(transactionService).deleteTransaction(id);

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void filterTransactionsByType_ReturnsList() throws Exception {

        TransactionViewDTO t1 = new TransactionViewDTO();
        t1.setType(TransactionType.INCOME);

        when(transactionService.getTransactionsByType(TransactionType.INCOME))
                .thenReturn(List.of(t1));

        mockMvc.perform(get("/api/transactions/filter")
                    .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void sortTransactions_ReturnsList() throws Exception {

        TransactionViewDTO t1 = new TransactionViewDTO();
        t1.setAmount(new BigDecimal("300.00"));
        TransactionViewDTO t2 = new TransactionViewDTO();
        t2.setAmount(new BigDecimal("100.00"));

        when(transactionService.getSortedTransactions("amount", "asc"))
                .thenReturn(List.of(t2, t1));

        mockMvc.perform(get("/api/transactions/sort")
                        .param("sortBy", "amount")
                        .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
