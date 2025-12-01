package org.example.microserviceaccount.repository;

import org.example.microserviceaccount.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUserName(String userName);
    List<Account> findByUserNameContainingIgnoreCase(String usernameFragment);

    /// PROCEDURI STOCATE
    // Count new users in a time frame
    @Query(value = "SELECT account_schema.count_new_users(:startDate, :endDate)", nativeQuery = true)
    Integer countNewUsers(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Anonymize user data
    @Query(value = "SELECT account_schema.anonymize_user_data(:userId)", nativeQuery = true)
    String anonymizeUserData(@Param("userId") Long userId);

    // Check account availability
    @Query(value = "SELECT account_schema.check_account_availability(:email, :username)", nativeQuery = true)
    String checkAccountAvailability(@Param("email") String email, @Param("username") String username);
}