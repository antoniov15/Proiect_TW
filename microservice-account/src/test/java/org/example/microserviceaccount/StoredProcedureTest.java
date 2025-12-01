package org.example.microserviceaccount;

import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.repository.AccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
// Asigură-te că testele rulează pe profilul care se conectează la Postgres, nu H2
// Dacă ai un application-test.properties setat pe H2, comentează linia de mai jos sau configurează-l pe Postgres
// @ActiveProfiles("test")
@Transactional
public class StoredProcedureTest {
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        // Curățăm baza de date (opțional, @Transactional se ocupă de obicei de asta)
        // accountRepository.deleteAll();
    }

    @Test
    public void testCheckAvailability(){
        Account account = new Account();
        account.setUserName("testUserUnique");
        account.setEmail("test@unique.com");
        account.setPassword("pass123");
        account.setRole("USER");
        accountRepository.save(account);

        // user luat
        String resultUserTaken = accountRepository.checkAccountAvailability("new@mail.com", "testUserUnique");
        assertEquals("USERNAME_TAKEN", resultUserTaken, "Procedura ar trebui să detecteze username-ul existent");

        // mail luat
        String resultEmailTaken = accountRepository.checkAccountAvailability("test@unique.com", "newUserUnique");
        assertEquals("EMAIL_TAKEN", resultEmailTaken, "Procedura ar trebui să detecteze email-ul existent");

        // totul liber
        String resultAvailable = accountRepository.checkAccountAvailability("fresh@mail.com", "freshUser");
        assertEquals("AVAILABLE", resultAvailable, "Procedura ar trebui să returneze AVAILABLE");
    }

    @Test
    public void testCountNewUsers(){
        Account acc1 = new Account(null, "user1", "u1@test.com", "pass", null, "USER");
        Account acc2 = new Account(null, "user2", "u2@test.com", "pass", null, "USER");

        accountRepository.save(acc1);
        accountRepository.save(acc2);

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

        String result = accountRepository.anonymizeUserData(userId);

        assertEquals("SUCCESS", result);

        Account updatedAccount = accountRepository.findById(userId).orElseThrow();

        assertEquals("DeletedUser_" + userId, updatedAccount.getUserName());
        assertEquals("deleted_" + userId + "@financeapp.local", updatedAccount.getEmail());
        assertEquals("deleted", updatedAccount.getPassword());
        assertEquals("INACTIVE", updatedAccount.getRole());
    }

    @Test
    public void testAnonymizeUserNotFound() {
        // Apelăm procedura pentru un ID inexistent
        String result = accountRepository.anonymizeUserData(999999L);
        assertEquals("USER_NOT_FOUND", result);
    }
}
