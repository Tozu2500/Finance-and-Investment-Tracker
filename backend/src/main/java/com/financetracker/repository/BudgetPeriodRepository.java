package com.financetracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.financetracker.model.BudgetPeriod;
import com.financetracker.model.User;

public interface BudgetPeriodRepository extends JpaRepository<BudgetPeriod, String> {

    List<BudgetPeriod> findByUserAndYearAndMonth(User user, int year, int month);

    List<BudgetPeriod> findByUserAndYear(User user, int year);

    Optional<BudgetPeriod> findByCategoryIdAndUserAndYearAndMonth(String categoryId, User user, int year, int month);

    @Query("SELECT bp FROM BudgetPeriod bp WHERE bp.user = :user AND bp.year = :year AND bp.month = :month")
    List<BudgetPeriod> findAllForMonth(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    Optional<BudgetPeriod> findByIdAndUser(String id, User user);

}
