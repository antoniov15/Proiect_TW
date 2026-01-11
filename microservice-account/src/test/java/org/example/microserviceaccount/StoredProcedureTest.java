package org.example.microserviceaccount;

import jakarta.persistence.EntityManager;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
// @ActiveProfiles("test") // postgres, nu H2
@Transactional
class StoredProcedureTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        // Optional cleanup
    }

    @Test
    public void testCheckAvailability(){
        Account account = new Account();
        account.setUserName("testUserUnique");
        account.setEmail("test@unique.com");
        account.setPassword("pass123");
        account.setRole("USER");
        accountRepository.save(account);

        accountRepository.flush();

        String resultUserTaken = accountRepository.checkAccountAvailability("new@mail.com", "testUserUnique");
        assertEquals("USERNAME_TAKEN", resultUserTaken, "Procedura ar trebui să detecteze username-ul existent");

        String resultEmailTaken = accountRepository.checkAccountAvailability("test@unique.com", "newUserUnique");
        assertEquals("EMAIL_TAKEN", resultEmailTaken, "Procedura ar trebui să detecteze email-ul existent");

        String resultAvailable = accountRepository.checkAccountAvailability("fresh@mail.com", "freshUser");
        assertEquals("AVAILABLE", resultAvailable, "Procedura ar trebui să returneze AVAILABLE");
    }

    @Test
    public void testCountNewUsers(){
        Account acc1 = new Account(null, "user1", "u1@test.com", "pass", null, "USER");
        Account acc2 = new Account(null, "user2", "u2@test.com", "pass", null, "USER");

        accountRepository.save(acc1);
        accountRepository.save(acc2);
        accountRepository.flush();

        LocalDate today = LocalDate.now();

        Integer count = accountRepository.countNewUsers(today.minusDays(1), today.plusDays(1));

        assertTrue(count >= 2, "Cel putin 2 useri");
    }

    @Test
    public void testAnonymizeUserData() {
        Account account = new Account();
        account.setUserName("gdprUser");
        account.setEmail("gdpr@original.com");
        account.setPassword("secretPass");
        account.setRole("USER");
        Account savedAccount = accountRepository.save(account);
        Long userId = savedAccount.getId();
        accountRepository.flush();

        String result = accountRepository.anonymizeUserData(userId);
        assertEquals("SUCCESS", result);

        entityManager.clear();

        Account updatedAccount = accountRepository.findById(userId).orElseThrow();

        assertEquals("DeletedUser_" + userId, updatedAccount.getUserName());
        assertEquals("deleted_" + userId + "@financeapp.local", updatedAccount.getEmail());
        assertEquals("deleted", updatedAccount.getPassword());
        assertEquals("INACTIVE", updatedAccount.getRole());
    }

    @Test
    public void testAnonymizeUserNotFound() {
        String result = accountRepository.anonymizeUserData(999999L);
        assertEquals("USER_NOT_FOUND", result);
    }
}