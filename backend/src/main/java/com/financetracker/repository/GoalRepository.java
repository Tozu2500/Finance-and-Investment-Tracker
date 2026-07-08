package com.financetracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financetracker.model.Goal;
import com.financetracker.model.User;

public interface GoalRepository extends JpaRepository<Goal, String> {

    List<Goal> findByUserOrderByDeadlineAsc(User user);

    Optional<Goal> findByIdAndUser(String id, User user);

    void deleteAllByUser(User user);

}
