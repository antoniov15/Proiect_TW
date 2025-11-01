package org.example.microserviceaccount.repository;

import org.example.microserviceaccount.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUserName(String userName);

    List<Account> findByUserNameContainingIgnoreCase(String usernameFragment);
}