package com.financetracker.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;

import jakarta.transaction.Transactional;

public interface TransactionRepository extends JpaRepository<Transaction, String>, 
                    JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUser(String id, User user);

    @Query("SELECT t from Transaction t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.account " +
            "WHERE t.user = :user AND t.isDeleted = false ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findRecentByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT COALESCE(" +
           "  SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) - " +
           "  SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END)" +
           ", 0) FROM Transaction t WHERE t.user = :user AND t.date <= :endDate AND t.isDeleted = false")
    BigDecimal sumSignedAmountsUntil(@Param("user") User user, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.user = :user AND t.type = :type AND t.isDeleted = false " +
        "AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    BigDecimal sumByTypeAndYearMonth(@Param("user") User user,
                                @Param("type") TransactionType type,
                                @Param("year") int year,
                                @Param("month") int month);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.user = :user AND t.type = 'EXPENSE' AND t.isDeleted = false " +
        "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
        "GROUP BY t.category ORDER BY COALESCE(SUM(t.amount), 0) DESC")
    List<Object[]> findCategoryBreakdown(@Param("user") User user,
                                        @Param("year") int year,
                                        @Param("month") int month);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.account " +
        "WHERE t.user = :user AND t.isDeleted = false AND t.type = 'EXPENSE' " +
        "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
        "ORDER BY t.amount DESC")
    List<Transaction> findLargestExpenseInMonth(@Param("user") User user,
                                            @Param("year") int year,
                                            @Param("month") int month,
                                            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.isDeleted = false " +
        "AND t.date >= :from AND t.date <= :to")
    List<Transaction> findByUserAndDateBetween(@Param("user") User user,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.account " +
        "WHERE t.user = :user AND t.isDeleted = false ORDER BY t.date DESC, t.id DESC")
    List<Transaction> findByUserWithRelations(@Param("user") User user);

    List<Transaction> findByUserAndIsDeletedFalse(User user);
    void deleteAllByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Transaction t SET t.category = null WHERE t.category.id = :categoryId AND t.user = :user")
    void nullifyCategoryReferences(@Param("categoryId") String categoryId, @Param("user") User user);

    @Transactional
    @Modifying
    @Query("UPDATE Transaction t SET t.account = null WHERE t.account.id = :accountId AND t.user = :user")
    void nullifyAccountReferences(@Param("accountId") String accountId, @Param("user") User user);
    
    @Query("SELECT DAYOFWEEK(t.date), COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = 'EXPENSE' AND t.isDeleted = false " +
           "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
           "GROUP BY DAYOFWEEK(t.date)")
    List<Object[]> findDayOfWeekSpend(@Param("user") User user,
                                    @Param("year") int year,
                                    @Param("month") int month);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user AND t.isDeleted = false " +
        "AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    int countByUserAndYearMonth(@Param("user") User user,
                                @Param("year") int year,
                                @Param("month") int month);

    // Query replacing the "per-month" sumByTypeAndYearMonth loop 'trending'
    @Query("SELECT YEAR(t.date), MONTH(t.date), t.type, COALESCE(SUM(t.amount), 0) " +
        "FROM Transaction t WHERE t.user = :user AND t.isDeleted = false " +
        "AND t.date >= :from " +
        "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
        "GROUP BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> sumByYearMonthAndType(@Param("user") User user, @Param("from") LocalDate from);
    
}
