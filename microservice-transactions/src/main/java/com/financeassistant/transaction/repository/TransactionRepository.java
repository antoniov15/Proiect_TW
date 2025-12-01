package com.financeassistant.transaction.repository;

import com.financeassistant.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByUserId(Long userId);

    @Query(value = "SELECT transaction_schema.calculate_monthly_expense(:userId, :month, :year)", nativeQuery = true)
    BigDecimal calculateMonthlyExpense(@Param("userId") Long userId,
                                       @Param("month") int month,
                                       @Param("year") int year);

    @Query(value = "SELECT transaction_schema.check_budget_status(:userId, :budgetLimit)", nativeQuery = true)
    String checkBudgetStatus(@Param("userId") Long userId,
                             @Param("budgetLimit") BigDecimal budgetLimit);

    @Query(value = "SELECT transaction_schema.archive_old_transactions(:cutoffDate)", nativeQuery = true)
    Integer archiveOldTransactions(@Param("cutoffDate") LocalDate cutoffDate);
}