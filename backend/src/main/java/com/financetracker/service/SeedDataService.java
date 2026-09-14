package com.financetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financetracker.model.Account;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
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

    // Deletes all data for a user in dependency order
    @Transactional
    public void clearUserData(User user) {
        goalDepositRepo.deleteAllByUser(user);
        transactionRepo.deleteAllByUser(user);
        recurringRuleRepo.deleteAllByUser(user);
        goalRepo.deleteAllByUser(user);
        accountRepo.deleteAllByUser(user);
        categoryRepo.deleteAllByUser(user);
    }

    @Transactional
    public void seedNewUser(User user) {
        List<Category> cats = seedCategories(user);
        List<Account> accs = seedAccounts(user);
        seedTransactions(user, cats, accs);
    }

    private List<Category> seedCategories(User user) {
        List<Category> list = new ArrayList<>();

        // Income
        list.add(cat(user, "Salary",              TransactionType.INCOME,  "#22c55e", "💼", 0));
        list.add(cat(user, "Freelance",            TransactionType.INCOME,  "#16a34a", "💻", 0));
        list.add(cat(user, "Investment",           TransactionType.INCOME,  "#15803d", "📈", 0));
        list.add(cat(user, "Bonus",                TransactionType.INCOME,  "#14532d", "🎁", 0));
        list.add(cat(user, "Rental Income",        TransactionType.INCOME,  "#10b981", "🏠", 0));
        list.add(cat(user, "Side Business",        TransactionType.INCOME,  "#059669", "🛍", 0));
        list.add(cat(user, "Tax Refund",           TransactionType.INCOME,  "#34d399", "🧾", 0));
        list.add(cat(user, "Gift",                 TransactionType.INCOME,  "#6ee7b7", "🎀", 0));
        list.add(cat(user, "Government Benefits",  TransactionType.INCOME,  "#2dd4bf", "🤝", 0));
        list.add(cat(user, "Pension",              TransactionType.INCOME,  "#818cf8", "🏦", 0));
        list.add(cat(user, "Child Support",        TransactionType.INCOME,  "#f472b6", "👶", 0));
        list.add(cat(user, "Other Income",         TransactionType.INCOME,  "#a7f3d0", "💰", 0));

        // Expense
        list.add(cat(user, "Rent / Mortgage",    TransactionType.EXPENSE, "#6366f1", "🏠", 1500));
        list.add(cat(user, "Groceries",          TransactionType.EXPENSE, "#fb923c", "🛒", 600));
        list.add(cat(user, "Dining Out",         TransactionType.EXPENSE, "#f97316", "🍔", 400));
        list.add(cat(user, "Coffee & Cafes",     TransactionType.EXPENSE, "#d97706", "☕",   80));
        list.add(cat(user, "Transport",          TransactionType.EXPENSE, "#3b82f6", "🚗", 300));
        list.add(cat(user, "Utilities",          TransactionType.EXPENSE, "#8b5cf6", "⚡", 200));
        list.add(cat(user, "Phone / Mobile",     TransactionType.EXPENSE, "#60a5fa", "📞",  60));
        list.add(cat(user, "Clothing",           TransactionType.EXPENSE, "#f87171", "👕", 150));
        list.add(cat(user, "Childcare",          TransactionType.EXPENSE, "#fb923c", "🧒",   0));
        list.add(cat(user, "Loan Repayment",     TransactionType.EXPENSE, "#94a3b8", "💸",   0));
        list.add(cat(user, "Health & Medical",   TransactionType.EXPENSE, "#ec4899", "🏥", 200));
        list.add(cat(user, "Entertainment",      TransactionType.EXPENSE, "#f59e0b", "🎬", 200));
        list.add(cat(user, "Shopping",           TransactionType.EXPENSE, "#ef4444", "🛍", 300));
        list.add(cat(user, "Education",          TransactionType.EXPENSE, "#06b6d4", "📚", 150));
        list.add(cat(user, "Travel",             TransactionType.EXPENSE, "#14b8a6", "✈️", 300));
        list.add(cat(user, "Subscriptions",      TransactionType.EXPENSE, "#a855f7", "📺", 100));
        list.add(cat(user, "Personal Care",      TransactionType.EXPENSE, "#db2777", "💇", 100));
        list.add(cat(user, "Insurance",          TransactionType.EXPENSE, "#7c3aed", "🛡️", 200));
        list.add(cat(user, "Pets",               TransactionType.EXPENSE, "#65a30d", "🐶", 100));
        list.add(cat(user, "Gaming",             TransactionType.EXPENSE, "#0369a1", "🎮",  60));
        list.add(cat(user, "Sports & Fitness",   TransactionType.EXPENSE, "#0891b2", "🏋️",  80));
        list.add(cat(user, "Charity",            TransactionType.EXPENSE, "#dc2626", "❤️",  50));
        list.add(cat(user, "Home Improvement",   TransactionType.EXPENSE, "#92400e", "🔨", 200));
        list.add(cat(user, "Bank Fees",          TransactionType.EXPENSE, "#374151", "🏦",  20));
        list.add(cat(user, "Other",              TransactionType.EXPENSE, "#9ca3af", "📋",   0));

        // Not spending — money changing shape, not leaving. See Category.excludeFromSpending.
        list.add(cat(user, "Investing",          TransactionType.EXPENSE, "#0ea5e9", "📈",   0, true));

        return categoryRepo.saveAll(list);
    }

    private List<Account> seedAccounts(User user) {
        List<Account> list = List.of(
                Account.builder().user(user).name("Main Checking").icon("🏦").openingBalance(BigDecimal.valueOf(4500.0)).build(),
                Account.builder().user(user).name("Savings").icon("💰").openingBalance(BigDecimal.valueOf(12000.0)).build(),
                Account.builder().user(user).name("Credit Card").icon("💳").openingBalance(BigDecimal.valueOf(-800.0)).build()
        );
        return accountRepo.saveAll(list);
    }

    private void seedTransactions(User user, List<Category> cats, List<Account> accs) {
        if (accs.isEmpty() || cats.isEmpty()) return;

        Account checking = accs.get(0);
        Account savings  = accs.get(1);
        Account credit   = accs.get(2);

        Category salary    = findCat(cats, "Salary");
        Category dining    = findCat(cats, "Dining Out");
        Category groceries = findCat(cats, "Groceries");
        Category transport = findCat(cats, "Transport");
        Category rent      = findCat(cats, "Rent / Mortgage");
        Category utilities = findCat(cats, "Utilities");
        Category health    = findCat(cats, "Health & Medical");
        Category entertain = findCat(cats, "Entertainment");
        Category subs      = findCat(cats, "Subscriptions");
        Category coffee    = findCat(cats, "Coffee & Cafes");
        Category shopping  = findCat(cats, "Shopping");

        Random random = new Random(42);
        List<Transaction> txns = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Generate 4 months of realistic transactions
        for (int m = 3; m >= 0; m--) {
            LocalDate base = today.withDayOfMonth(1).minusMonths(m);

            // Monthly salary on the 1st
            txns.add(tx(user, base.withDayOfMonth(1),  4800.0 + rng.nextInt(400),  TransactionType.INCOME,  salary,    checking, "Monthly salary"));

            // Rent on the 1st
            txns.add(tx(user, base.withDayOfMonth(1),  1350.0,  TransactionType.EXPENSE, rent,      checking, "Rent"));

            // Utilities mid-month
            txns.add(tx(user, base.withDayOfMonth(15), 85.0 + rng.nextInt(30),  TransactionType.EXPENSE, utilities, checking, "Electricity bill"));
            txns.add(tx(user, base.withDayOfMonth(18), 35.0 + rng.nextInt(10),  TransactionType.EXPENSE, utilities, checking, "Internet bill"));

            // Groceries — weekly
            for (int w = 0; w < 4; w++) {
                int day = 3 + w * 7;
                if (day <= daysInMonth(base)) {
                    txns.add(tx(user, base.withDayOfMonth(day), 80.0 + rng.nextInt(60),  TransactionType.EXPENSE, groceries, credit, "Weekly grocery run"));
                }
            }

            // Dining out
            txns.add(tx(user, base.withDayOfMonth(5),  18.0 + rng.nextInt(20),  TransactionType.EXPENSE, dining,    credit,   "Lunch"));
            txns.add(tx(user, base.withDayOfMonth(12), 45.0 + rng.nextInt(30),  TransactionType.EXPENSE, dining,    credit,   "Dinner out"));
            txns.add(tx(user, base.withDayOfMonth(20), 22.0 + rng.nextInt(15),  TransactionType.EXPENSE, dining,    credit,   "Restaurant"));

            // Coffee
            for (int d = 0; d < 8; d++) {
                int day = 2 + d * 3 + rng.nextInt(2);
                if (day <= daysInMonth(base)) {
                    txns.add(tx(user, base.withDayOfMonth(day), 4.5 + rng.nextInt(3),  TransactionType.EXPENSE, coffee, credit, "Coffee"));
                }
            }

            // Transport
            txns.add(tx(user, base.withDayOfMonth(7),  55.0 + rng.nextInt(30),  TransactionType.EXPENSE, transport, checking, "Public transport"));
            txns.add(tx(user, base.withDayOfMonth(22), 40.0 + rng.nextInt(20),  TransactionType.EXPENSE, transport, checking, "Fuel"));

            // Subscriptions
            txns.add(tx(user, base.withDayOfMonth(2),  15.99,  TransactionType.EXPENSE, subs, credit, "Netflix"));
            txns.add(tx(user, base.withDayOfMonth(3),  9.99,   TransactionType.EXPENSE, subs, credit, "Spotify"));

            // Health
            if (m % 2 == 0) {
                txns.add(tx(user, base.withDayOfMonth(14), 120.0 + rng.nextInt(80), TransactionType.EXPENSE, health, checking, "Doctor visit"));
            }

            // Entertainment
            txns.add(tx(user, base.withDayOfMonth(10), 25.0 + rng.nextInt(30), TransactionType.EXPENSE, entertain, credit, "Cinema"));

            // Shopping
            txns.add(tx(user, base.withDayOfMonth(25), 60.0 + rng.nextInt(100), TransactionType.EXPENSE, shopping, credit, "Clothing"));

            // Savings transfer
            txns.add(tx(user, base.withDayOfMonth(28), 500.0,  TransactionType.INCOME, findCat(cats, "Other Income"), savings, "Monthly savings transfer"));

            // Investing — excluded from spending, so it shows up as "invested", not "spent"
            txns.add(tx(user, base.withDayOfMonth(5), 300.0, TransactionType.EXPENSE, findCat(cats, "Investing"), checking, "Index fund purchase"));
        }

        transactionRepo.saveAll(txns);
    }

    // Helper methods
    
}
