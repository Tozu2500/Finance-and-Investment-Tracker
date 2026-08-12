package com.financetracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financetracker.dto.RecurringRuleDto;
import com.financetracker.model.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.RecurringRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecurringRuleService {

    private final RecurringRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<RecurringRuleDto> getAll(User user) {
        return ruleRepository.findByUserOrderByNameAsc(user)
            .stream().map(RecurringRuleDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RecurringRuleDto> getActive(User user) {
        return ruleRepository.findByUserAndIsActiveTrueOrderByNextDueDateAsc(user)
            .stream().map(RecurringRuleDto::from).toList();
    }

    
}
