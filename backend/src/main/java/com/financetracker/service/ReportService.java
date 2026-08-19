package com.financetracker.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.financetracker.dto.report.DashboardDto;
import com.financetracker.model.Account;
import com.financetracker.model.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;

import jakarta.persistence.Cacheable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final AccountService accountService;

    @Cacheable(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public DashboardDto getDashboard(User user) {
        List<Account> accounts = accountRepository.findByUserOrderBySortOrderAscNameAsc(user);

        double openingSum = accounts.stream()
                .mapToDouble(a -> a.getOpeningBalance() != null ? a.getOpeningBalance().doubleValue() : 0.0)
                .sum();
    }

}
