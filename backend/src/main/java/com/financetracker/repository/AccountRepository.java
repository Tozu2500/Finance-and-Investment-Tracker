package com.financetracker.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.financetracker.model.Account;
import com.financetracker.model.User;

import jakarta.transaction.Transactional;

public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByUserAndIsArchivedFalseOrderBySortOrderAscNameAsc(User user);

    List<Account> findByUserOrderBySortOrderAscNameAsc(User user);

    Optional<Account> findByIdAndUser(String id, User user);

    Optional<Account> findByUserAndIsDefaultTrue(User user);
    
    void deleteAllByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Account a SET a.isDefault = false WHERE a.user = :user")
    void clearDefaultForUser(@Param("user") User user);

    @Query("SELECT COALESCE(" +
           "  SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) - " +
           "  SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END)" +
           ", 0) FROM Transaction t WHERE t.account = :account AND t.user = :user AND t.isDeleted = false")

    BigDecimal sumSignedAmountsForAccount(@Param("account") Account account, @Param("user") User user);
    
}
