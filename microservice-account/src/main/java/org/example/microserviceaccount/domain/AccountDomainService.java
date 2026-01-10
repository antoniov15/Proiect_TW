package org.example.microserviceaccount.domain;

import org.example.microserviceaccount.entity.Account;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Service
public class AccountDomainService {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Validates account creation business rules
     */
    public void validateAccountCreation(String userName, String email, String password) {
        validateUsername(userName);
        validateEmail(email);
        validatePassword(password);
    }

    /**
     * Validates username according to business rules
     */
    public void validateUsername(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (userName.length() < MIN_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Username must be at least " + MIN_USERNAME_LENGTH + " characters long"
            );
        }

        if (userName.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters"
            );
        }

        // Username should contain only alphanumeric characters and underscores
        if (!userName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException(
                    "Username can only contain letters, numbers, and underscores"
            );
        }
    }

    /**
     * Validates email according to business rules
     */
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validates password according to business rules
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long"
            );
        }

        // Additional password strength rules
        if (!password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    /**
     * Validates account update business rules
     */
    public void validateAccountUpdate(Account existingAccount, String newUserName, String newEmail) {
        if (existingAccount == null) {
            throw new IllegalArgumentException("Existing account cannot be null");
        }

        if (newUserName != null && !newUserName.equals(existingAccount.getUserName())) {
            validateUsername(newUserName);
        }

        if (newEmail != null && !newEmail.equals(existingAccount.getEmail())) {
            validateEmail(newEmail);
        }
    }

    /**
     * Validates date range for user count statistics
     */
    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }

        // Maximum range: 1 year
        if (startDate.plusYears(1).isBefore(endDate)) {
            throw new IllegalArgumentException("Date range cannot exceed 1 year");
        }
    }

    /**
     * Validates anonymization request
     */
    public void validateAnonymizationRequest(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
    }

    /**
     * Checks if role is valid
     */
    public void validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }

        // Valid roles: USER, ADMIN, INACTIVE
        if (!role.matches("^(USER|ADMIN|INACTIVE)$")) {
            throw new IllegalArgumentException(
                    "Invalid role. Must be one of: USER, ADMIN, INACTIVE"
            );
        }
    }

    /**
     * Business rule: Check if account can be deleted
     * For example, admin accounts might require special handling
     */
    public void validateAccountDeletion(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        // Example business rule: Cannot delete the last admin account
        if ("ADMIN".equals(account.getRole())) {
            // This would typically check if there are other admin accounts
            // For now, we just allow it, but you could add additional checks
        }

        // Example: Cannot delete accounts less than 24 hours old
        if (account.getCreatedAt() != null &&
                account.getCreatedAt().isAfter(LocalDate.now().minusDays(1))) {
            throw new IllegalArgumentException(
                    "Cannot delete account created less than 24 hours ago"
            );
        }
    }


    //Validates password reset request
    public void validatePasswordReset(String email, String newPassword) {
        validateEmail(email);
        validatePassword(newPassword);
    }

    //Check if username is appropriate
    public void validateUsernameContent(String userName) {
        // Reserved usernames
        String[] reservedNames = {"admin", "root", "system", "support", "help"};

        for (String reserved : reservedNames) {
            if (userName.equalsIgnoreCase(reserved)) {
                throw new IllegalArgumentException(
                        "Username '" + userName + "' is reserved and cannot be used"
                );
            }
        }
    }
}
