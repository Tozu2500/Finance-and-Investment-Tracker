package com.financetracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.financetracker.dto.BudgetPeriodDto;
import com.financetracker.model.BudgetPeriod;
import com.financetracker.model.Category;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetPeriodRepository;
import com.financetracker.repository.CategoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetPeriodService {

    private final BudgetPeriodRepository budgetPeriodRepository;
    private final CategoryRepository categoryRepository;

    public List<BudgetPeriodDto> getForMonth(User user, int year, int month) {
        return budgetPeriodRepository.findAllForMonth(user, year, month).stream()
                .map(BudgetPeriod::from).toList();
    }

    public List<BudgetPeriodDto> getForYear(User user, int year) {
        return budgetPeriodRepository.findByUserAndYear(user, year).stream()
            .map(BudgetPeriodDto::from).toList();
    }

    @Transactional
    public BudgetPeriodDto upsert(User user, BudgetPeriodDto dto) {
        Category category = categoryRepository.findByIdAndUser(dto.categoryId(), user)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    
        BudgetPeriod bp = budgetPeriodRepository
            .findByCategoryIdAndUserAndYearAndMonth(dto.categoryId(), user, dto.year(), dto.month())
            .orElseGet(() -> BudgetPeriod.builder()
                .category(category)
                .user(user)
                .year(dto.year())
                .month(dto.month())
                .build());

        bp.setAmount(BigDecimal.valueOf(dto.amount()));
        bp.setNote(dto.note());

        return BudgetPeriodDto.from(budgetPeriodRepository.save(bp));
    }

    @Transactional
    public void delete(String id, User user) {
        BudgetPeriod bp = budgetPeriodRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Budget period not found"));
        budgetPeriodRepository.deleteById(bp.getId());
    }
}
