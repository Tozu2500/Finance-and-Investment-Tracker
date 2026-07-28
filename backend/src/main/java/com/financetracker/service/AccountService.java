package com.financetracker.service;

import java.math.BigDecimal;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.financetracker.model.Account;
import com.financetracker.model.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.RecurringRuleRepository;
import com.financetracker.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringRuleRepository recurringRuleRepository;

    /**
     * Every transaction requires an account, so a user with zero accounts can
     * never add one — the add-transaction forms have no account to offer and
     * stay permanently disabled. Called on registration and on every login so
     * it also self-heals existing accounts (e.g. the seeded demo user) that
     * predate this method.
     */
    @CacheEvict(value = CACHE_DASHBOARD, key = "#user_id")
    @Transactional
    public void seedDefaultIfMissing(User user) {
        if (!accountRepository.findByUserOrderBySortOrderAscNameAsc(user).isEmpty()) {
            return;
        }

        Account account = Account.builder()
            .name("Cash")
            .icon("💵")
            .accountType("CASH")
            .openingBalance(BigDecimal.ZERO)
            .colorHex("#10b981")
            .isDefault(true)
            .sortOrder(0)
            .user(user)
            .build();
        accountRepository.save(account);
    }

    
}
