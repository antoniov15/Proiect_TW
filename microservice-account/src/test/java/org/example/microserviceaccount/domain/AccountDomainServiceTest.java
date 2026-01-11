package org.example.microserviceaccount.domain;

import org.example.microserviceaccount.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AccountDomainServiceTest {

    private AccountDomainService accountDomainService;

    @BeforeEach
    void setUp() {
        accountDomainService = new AccountDomainService();
    }

    // Username Validation Tests

    @Test
    @DisplayName("Should pass when username is valid")
    void testValidateUsername_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validateUsername("validUser123"));
    }

    @Test
    @DisplayName("Should throw exception when username is null")
    void testValidateUsername_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateUsername(null));
        assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when username is too short")
    void testValidateUsername_TooShort() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateUsername("ab"));
        assertTrue(exception.getMessage().contains("at least 3 characters"));
    }

    @Test
    @DisplayName("Should throw exception when username is too long")
    void testValidateUsername_TooLong() {
        String longUsername = "a".repeat(51);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateUsername(longUsername));
        assertTrue(exception.getMessage().contains("cannot exceed 50 characters"));
    }

    @Test
    @DisplayName("Should throw exception when username contains special characters")
    void testValidateUsername_SpecialCharacters() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateUsername("user@name"));
        assertTrue(exception.getMessage().contains("letters, numbers, and underscores"));
    }

    // Email Validation Tests

    @Test
    @DisplayName("Should pass when email is valid")
    void testValidateEmail_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validateEmail("user@example.com"));
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void testValidateEmail_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateEmail(null));
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when email format is invalid")
    void testValidateEmail_InvalidFormat() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateEmail("invalid-email"));
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when email missing @ symbol")
    void testValidateEmail_MissingAtSymbol() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateEmail("userexample.com"));
        assertEquals("Invalid email format", exception.getMessage());
    }

    // Password Validation Tests

    @Test
    @DisplayName("Should pass when password is valid")
    void testValidatePassword_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validatePassword("Pass123"));
    }

    @Test
    @DisplayName("Should throw exception when password is null")
    void testValidatePassword_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validatePassword(null));
        assertEquals("Password cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is too short")
    void testValidatePassword_TooShort() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validatePassword("Pa1"));
        assertTrue(exception.getMessage().contains("at least 6 characters"));
    }

    @Test
    @DisplayName("Should throw exception when password has no letters")
    void testValidatePassword_NoLetters() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validatePassword("123456"));
        assertEquals("Password must contain at least one letter", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password has no digits")
    void testValidatePassword_NoDigits() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validatePassword("Password"));
        assertEquals("Password must contain at least one digit", exception.getMessage());
    }

    // Account Creation Validation Tests

    @Test
    @DisplayName("Should pass when account creation data is valid")
    void testValidateAccountCreation_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validateAccountCreation(
                "validUser", "user@example.com", "Pass123"
        ));
    }

    @Test
    @DisplayName("Should throw exception when account creation has invalid username")
    void testValidateAccountCreation_InvalidUsername() {
        assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateAccountCreation(
                        "ab", "user@example.com", "Pass123"
                ));
    }

    // Account Update Validation Tests

    @Test
    @DisplayName("Should pass when account update is valid")
    void testValidateAccountUpdate_Valid() {
        Account account = new Account();
        account.setUserName("oldUser");
        account.setEmail("old@example.com");

        assertDoesNotThrow(() -> accountDomainService.validateAccountUpdate(
                account, "newUser", "new@example.com"
        ));
    }

    @Test
    @DisplayName("Should throw exception when existing account is null")
    void testValidateAccountUpdate_NullAccount() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateAccountUpdate(
                        null, "newUser", "new@example.com"
                ));
        assertEquals("Existing account cannot be null", exception.getMessage());
    }

    // Date Range Validation Tests

    @Test
    @DisplayName("Should pass when date range is valid")
    void testValidateDateRange_Valid() {
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        assertDoesNotThrow(() -> accountDomainService.validateDateRange(startDate, endDate));
    }

    @Test
    @DisplayName("Should throw exception when start date is null")
    void testValidateDateRange_NullStartDate() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateDateRange(null, LocalDate.now()));
        assertEquals("Start date and end date cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when start date is after end date")
    void testValidateDateRange_StartAfterEnd() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateDateRange(startDate, endDate));
        assertEquals("Start date cannot be after end date", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when end date is in the future")
    void testValidateDateRange_FutureEndDate() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateDateRange(startDate, endDate));
        assertEquals("End date cannot be in the future", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when date range exceeds 1 year")
    void testValidateDateRange_ExceedsOneYear() {
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateDateRange(startDate, endDate));
        assertEquals("Date range cannot exceed 1 year", exception.getMessage());
    }

    // Anonymization Validation Tests

    @Test
    @DisplayName("Should pass when user ID is valid for anonymization")
    void testValidateAnonymizationRequest_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validateAnonymizationRequest(1L));
    }

    @Test
    @DisplayName("Should throw exception when user ID is null")
    void testValidateAnonymizationRequest_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateAnonymizationRequest(null));
        assertEquals("User ID must be a positive number", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user ID is negative")
    void testValidateAnonymizationRequest_Negative() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateAnonymizationRequest(-1L));
        assertEquals("User ID must be a positive number", exception.getMessage());
    }

    // Role Validation Tests

    @Test
    @DisplayName("Should pass when role is valid")
    void testValidateRole_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validateRole("USER"));
        assertDoesNotThrow(() -> accountDomainService.validateRole("ADMIN"));
        assertDoesNotThrow(() -> accountDomainService.validateRole("INACTIVE"));
    }

    @Test
    @DisplayName("Should throw exception when role is invalid")
    void testValidateRole_Invalid() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateRole("SUPERUSER"));
        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    // Account Deletion Validation Tests

    @Test
    @DisplayName("Should pass when account can be deleted")
    void testValidateAccountDeletion_Valid() {
        Account account = new Account();
        account.setId(1L);
        account.setRole("USER");
        account.setCreatedAt(LocalDate.now().minusDays(2));

        assertDoesNotThrow(() -> accountDomainService.validateAccountDeletion(account));
    }

    @Test
    @DisplayName("Should throw exception when account is null")
    void testValidateAccountDeletion_Null() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateAccountDeletion(null));
        assertEquals("Account cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when account is less than 24 hours old")
    void testValidateAccountDeletion_TooRecent() {
        Account account = new Account();
        account.setId(1L);
        account.setRole("USER");
        account.setCreatedAt(LocalDate.now());

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateAccountDeletion(account));
        assertTrue(exception.getMessage().contains("less than 24 hours ago"));
    }

    // Username Content Validation Tests

    @Test
    @DisplayName("Should pass when username is not reserved")
    void testValidateUsernameContent_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validateUsernameContent("regularUser"));
    }

    @Test
    @DisplayName("Should throw exception when username is reserved")
    void testValidateUsernameContent_Reserved() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateUsernameContent("admin"));
        assertTrue(exception.getMessage().contains("reserved"));
    }

    @Test
    @DisplayName("Should throw exception when username is reserved (case insensitive)")
    void testValidateUsernameContent_ReservedCaseInsensitive() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validateUsernameContent("ADMIN"));
        assertTrue(exception.getMessage().contains("reserved"));
    }

    // Password Reset Validation Tests

    @Test
    @DisplayName("Should pass when password reset data is valid")
    void testValidatePasswordReset_Valid() {
        assertDoesNotThrow(() -> accountDomainService.validatePasswordReset(
                "user@example.com", "NewPass123"
        ));
    }

    @Test
    @DisplayName("Should throw exception when password reset has invalid email")
    void testValidatePasswordReset_InvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> accountDomainService.validatePasswordReset(
                        "invalid-email", "NewPass123"
                ));
    }
}