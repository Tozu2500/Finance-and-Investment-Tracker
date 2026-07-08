package com.financetracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financetracker.model.Goal;
import com.financetracker.model.GoalDeposit;
import com.financetracker.model.User;

public interface GoalDepositRepository extends JpaRepository<GoalDeposit, String> {

    List<GoalDeposit> findByGoalAndUserOrderByCreatedAtDesc(Goal goal, User user);

    void deleteAllByUser(User user);

}
