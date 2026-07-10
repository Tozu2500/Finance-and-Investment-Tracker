package com.financetracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.financetracker.model.RecurringRule;
import com.financetracker.model.User;

import jakarta.transaction.Transactional;

public interface RecurringRuleRepository extends JpaRepository<RecurringRule, String> {

    List<RecurringRule> findByUserOrderByNameAsc(User user);

    List<RecurringRule> findByUserAndIsActiveTrueOrderByNextDueDateAsc(User user);

    Optional<RecurringRule> findByIdAndUser(String id, User user);

    void deleteAllByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE RecurringRule r SET r.category = null WHERE r.category.id = :catId AND r.user = :user")
    void nullifyCategoryReference(@Param("catId") String catId, @Param("user") User user);

    @Transactional
    @Modifying
    @Query("UPDATE RecurringRule r SET r.account = null WHERE r.account.id = :accId AND r.user = :user")
    void nullifyAccountReferences(@Param("accId") String accId, @Param("user") User user);

}
