package org.example.microserviceaccount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.exception.ResourceNotFoundException;
import org.example.microserviceaccount.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createAccount_ReturnsCreatedAccount() throws Exception {
        AccountCreateDTO createDTO = new AccountCreateDTO();
        createDTO.setEmail("new@test.com");
        createDTO.setUserName("newuser");
        createDTO.setPassword("pass1234");

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("new@test.com");
        responseDTO.setUserName("newuser");

        when(accountService.createAccount(any(AccountCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.userName").value("newuser"));
    }
    @Test
    void createAccount_InvalidInput_ReturnsBadRequest() throws Exception {
        AccountCreateDTO invalidDTO = new AccountCreateDTO();
        invalidDTO.setEmail("bad@test.com");
        invalidDTO.setUserName("user");
        invalidDTO.setPassword("123");

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccountById_ReturnsOk() throws Exception {
        Long accountId = 1L;
        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setId(accountId);
        responseDTO.setUserName("existingUser");

        when(accountService.getAccountById(accountId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("existingUser"));
    }

    @Test
    void getAccountById_NotFound_Returns404() throws Exception {
        Long accountId = 999L;
        when(accountService.getAccountById(accountId))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNotFound());
    }

}
