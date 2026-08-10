package com.financetracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.financetracker.dto.GoalDepositDto;
import com.financetracker.dto.GoalDto;
import com.financetracker.model.Goal;
import com.financetracker.model.GoalDeposit;
import com.financetracker.model.User;
import com.financetracker.repository.GoalDepositRepository;
import com.financetracker.repository.GoalRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalDepositRepository goalDepositRepository;

    public List<GoalDto> getAll(User user) {
        return goalRepository.findByUserOrderByDeadlineAsc(user)
            .stream().map(GoalDto::from).toList();
    }

    @Transactional
    public GoalDto create(GoalDto req, User user) {
        Goal goal = Goal.builder()
            .name(req.name())
            .targetAmount(BigDecimal.valueOf(req.targetAmount()))
            .savedAmount(BigDecimal.valueOf(req.savedAmount()))
            .deadline(req.deadline())
            .icon(req.icon() != null ? req.icon() : "🎯")
            .colorHex(req.colorHex() != null ? req.colorHex() : "#6466f1")
            .description(req.description())
            .user(user)
            .build();
        return GoalDto.from(goalRepository.save(goal));
    }

    @Transactional
    public GoalDto update(String id, GoalDto req, User user) {
        Goal g = findOrThrow(id, user);
        g.setName(req.name());
        g.setTargetAmount(BigDecimal.valueOf(req.targetAmount()));
        g.setSavedAmount(BigDecimal.valueOf(req.savedAmount()));
        g.setDeadline(req.deadline());

        if (req.icon() != null) g.setIcon(req.icon());

        if (req.colorHex() != null) g.setColorHex(req.colorHex());

        g.setDescription(req.description());

        return GoalDto.from(goalRepository.save(g));
    }

    @Transactional
    public void delete(String id, User user) {
        Goal g = findOrThrow(id, user);
        goalRepository.deleteById(g.getId());
    }

    @Transactional
    public GoalDto deposit(String id, double amount, User user) {
        if (amount <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit amount must be positive");

        Goal g = findOrThrow(id, user);
        BigDecimal currentSaved = g.getSavedAmount() != null ? g.getSavedAmount() : BigDecimal.ZERO;
        BigDecimal depositAmount = BigDecimal.valueOf(amount);
        BigDecimal target = g.getTargetAmount() != null ? g.getTargetAmount() : BigDecimal.ZERO;

        // Cap at target so savedAmount does not exceed targetAmount
        BigDecimal actualDeposit = depositAmount.min(target.subtract(currentSaved).max(BigDecimal.ZERO));
        g.setSavedAmount(currentSaved.add(actualDeposit));

        // Record the deposit in history
        GoalDeposit record = GoalDeposit.builder()
            .goal(g)
            .user(user)
            .amount(depositAmount)
            .build();
        goalDepositRepository.save(record);

        return GoalDto.from(goalRepository.save(g));
    }

    public List<GoalDepositDto> getDeposits(String id, User user) {
        Goal g = findOrThrow(id, user);
        return goalDepositRepository.findByGoalAndUserOrderByCreatedAtDesc(g, user)
            .stream().map(GoalDepositDto::from).toList();
    }

    private Goal findOrThrow(String id, User user) {
        return goalRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
