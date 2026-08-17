package com.financetracker.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.financetracker.dto.RecurringRuleDto;
import com.financetracker.model.RecurringRule;
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

    @Transactional
    public RecurringRuleDto create(RecurringRuleDto req, User user) {
        RecurringRule rule = RecurringRule.builder()
            .name(req.name())
            .amount(BigDecimal.valueOf(req.amount()))
            .currency(req.currency() != null ? req.currency() : "USD")
            .type(req.type())
            .frequency(req.frequency())
            .dayOfMonth(req.dayOfMonth())
            .nextDueDate(req.nextDueDate())
            .note(req.note())
            .merchant(req.merchant())
            .isActive(true)
            .user(user)
            .build();

        if (req.category() != null && req.category().id() != null) {
            categoryRepository.findByIdAndUser(req.category().id(), user)
                    .ifPresent(rule::setCategory);
        }

        if (req.account() != null && req.account().id() != null) {
            accountRepository.findByIdAndUser(req.account().id(), user)
                    .ifPresent(rule::setAccount);
        }

        RecurringRule saved = ruleRepository.save(rule);
        return RecurringRuleDto.from(saved);
    }

    @Transactional
    public RecurringRuleDto update(String id, RecurringRuleDto req, User user) {
        RecurringRule rule = findOrThrow(id, user);
        rule.setName(req.name());
        rule.setAmount(BigDecimal.valueOf(req.amount()));

        if (req.currency() != null) {
            rule.setCurrency(req.currency());
        }

        rule.setType(req.type());
        rule.setFrequency(req.frequency());
        rule.setDayOfMonth(req.dayOfMonth());
        rule.setNextDueDate(req.nextDueDate());
        rule.setNote(req.note());
        rule.setMerchant(req.merchant());
        rule.setIsActive(req.isActive());

        rule.setCategory(req.category() != null && req.category().id() != null
                ? categoryRepository.findByIdAndUser(req.category().id(), user).orElse(null)
                : null);

        rule.setAccount(req.account() != null && req.account().id() != null
                ? accountRepository.findByIdAndUser(req.account().id(), user).orElse(null)
                : null);

        return RecurringRuleDto.from(ruleRepository.save(rule));
    }

    @Transactional
    public void delete(String id, User user) {
        RecurringRule rule = findOrThrow(id, user);
        ruleRepository.deleteById(Objects.requireNonNull(rule.getId()));
    }

    private RecurringRule findOrThrow(String id, User user) {
        return ruleRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + id));
    }
}
