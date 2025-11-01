package org.example.microserviceaccount.entity;

import org.example.microserviceaccount.entity.Account;
import org.sringframework.data.jpa.repository.JpaRepository;;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUserName(String userName);

    List<Account> findByUserNameContainingIgnoreCase(String usernameFragment);
}