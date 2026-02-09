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

    // --- LIST OPERATIONS ---
    @Test
    void getAllAccounts_ReturnsOk() throws Exception {
        java.util.List<AccountResponseDTO> list = java.util.Collections.singletonList(new AccountResponseDTO());
        when(accountService.getAllAccounts()).thenReturn(list);

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchAccountsByUsername_ReturnsOk() throws Exception {
        String query = "test";
        when(accountService.findAccountsByUsernameContaining(query))
                .thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/api/v1/accounts/search").param("username", query))
                .andExpect(status().isOk());
    }

    // --- UPDATE & DELETE ---
    @Test
    void updateAccount_ReturnsOk() throws Exception {
        Long id = 1L;
        AccountCreateDTO updateDTO = new AccountCreateDTO();
        updateDTO.setEmail("up@test.com");
        updateDTO.setUserName("upUser");
        // FIX: Parola trebuie să aibă minim 6 caractere pentru a trece de @Valid
        updateDTO.setPassword("password123");

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setUserName("upUser");

        when(accountService.updateAccount(eq(id), any(AccountCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/v1/accounts/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("upUser"));
    }

    @Test
    void deleteAccount_ReturnsNoContent() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/v1/accounts/{id}", id))
                .andExpect(status().isNoContent()); // 204
    }

    // --- AUTH ENDPOINTS ---
    @Test
    void login_ReturnsOk() throws Exception {
        org.example.microserviceaccount.dto.LoginRequestDTO loginDTO = new org.example.microserviceaccount.dto.LoginRequestDTO();
        loginDTO.setLoginIdentifier("test@test.com");
        loginDTO.setPassword("pass");

        AccountResponseDTO responseDTO = new AccountResponseDTO();
        responseDTO.setEmail("test@test.com");

        when(accountService.login(any(org.example.microserviceaccount.dto.LoginRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/accounts/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void resetPassword_ReturnsOk() throws Exception {
        org.example.microserviceaccount.dto.ResetPasswordDTO resetDTO = new org.example.microserviceaccount.dto.ResetPasswordDTO();
        resetDTO.setEmail("test@test.com");
        resetDTO.setNewPassword("newPass123");

        when(accountService.resetPassword(eq("test@test.com"), eq("newPass123")))
                .thenReturn(new AccountResponseDTO());

        mockMvc.perform(post("/api/v1/accounts/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetDTO)))
                .andExpect(status().isOk());
    }

    // --- VIZA 3 ENDPOINTS ---
    @Test
    void getAccountSummary_ReturnsOk() throws Exception {
        Long id = 1L;
        org.example.microserviceaccount.dto.AccountSummaryDTO summary = new org.example.microserviceaccount.dto.AccountSummaryDTO();
        summary.setAccountId(id);
        summary.setTotalBalanceCalculated(100.0);

        when(accountService.getAccountSummary(id)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/accounts/{id}/summary", id)
                        .header("X-Trace-Id", "trace-123")
                        .principal(new org.springframework.security.authentication.TestingAuthenticationToken("testUser", "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalanceCalculated").value(100.0));
    }

    @Test
    void promoteToVip_ReturnsOk() throws Exception {
        Long id = 1L;
        AccountResponseDTO response = new AccountResponseDTO();
        response.setUserName("User (VIP)");

        when(accountService.checkAndPromoteToVip(id)).thenReturn(response);

        mockMvc.perform(put("/api/v1/accounts/{id}/promote-vip", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("User (VIP)"));
    }
}
