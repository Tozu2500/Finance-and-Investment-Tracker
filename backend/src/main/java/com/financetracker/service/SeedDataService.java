package com.financetracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financetracker.model.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.GoalDepositRepository;
import com.financetracker.repository.GoalRepository;
import com.financetracker.repository.RecurringRuleRepository;
import com.financetracker.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class SeedDataService {

    private final CategoryRepository categoryRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final GoalRepository goalRepo;
    private final GoalDepositRepository goalDepositRepo;
    private final RecurringRuleRepository recurringRuleRepo;

    // Deletes all financial data for a user in dependency order
    @Transactional 
    public void clearUserData(User user) {
        goalDepositRepo.deleteAllByUser(user);
        transactionRepo.deleteAllByUser(user);
        recurringRuleRepo.deleteAllByUser(user);
        goalRepo.deleteAllByUser(user);
        accountRepo.deleteAllByUser(user);
        categoryRepo.deleteAllByUser(user);
    }

    

}
