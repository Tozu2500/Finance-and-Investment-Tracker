package com.financetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.financetracker.dto.AccountDto;
import com.financetracker.dto.CategoryDto;
import com.financetracker.dto.TransactionDto;
import com.financetracker.dto.report.DashboardDto;
import com.financetracker.dto.report.MonthlyReportDto;
import com.financetracker.model.Account;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
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
        BigDecimal rawTxSum = transactionRepository.sumSignedAmountsUntil(user, LocalDate.now());
        double totalBalance = openingSum + (rawTxSum != null ? rawTxSum.doubleValue() : 0.0);

        YearMonth now = YearMonth.now();

        // One batch query for the last 12 months of income or expense
        LocalDate trendStart = now.minusMonths(11).atDay(1);
        Map<String, double[]> monthlyMap = buildMonthlyMap(
            transactionRepository.sumByYearMonthAndType(user, trendStart));

        String nowKey = now.getYear() + "-" + now.getMonthValue();
        double[] nowRow = monthlyMap.getOrDefault(nowKey, new double[]{0, 0});
        double monthIncome = nowRow[0];
        double monthExpense = nowRow[1];
        double savingsRate = monthIncome > 0 ? Math.max(0, (monthIncome - monthExpense) / monthIncome * 100) : 0;

        // 12 month balance trend -- 1 pre trend query + Java cumulation
        BigDecimal preTrend = transactionRepository.sumSignedAmountsUntil(user, trendStart.minusDays(1));
        double runningBalance = openingSum + (preTrend != null ? preTrend.doubleValue() : 0.0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");

        List<DashboardDto.MonthPoint> trend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            String key = ym.getYear() + "-" + ym.getMonthValue();
            double[] row = monthlyMap.getOrDefault(key, new double[]{0, 0});
            runningBalance -= row[0] - row[1];   // inc - exp
            
            trend.add(new DashboardDto.MonthPoint(ym.format(fmt), runningBalance));
        }

        // Spending breakdown (curr. month)
        List<Object[]> breakdownRows = transactionRepository.findCategoryBreakdown(user, now.getYear(), now.getMonthValue());
        Map<String, Double> spentByCategoryId = new HashMap<>();
        List<DashboardDto.CategoryAmount> breakdown = new ArrayList<>();

        for (Object[] row : breakdownRows) {
            CategoryDto cat = row[0] instanceof Category c
                    ? CategoryDto.from(c)
                    : new CategoryDto("uncategorized", "Uncategorized", TransactionType.EXPENSE, "#9ca3af", "📦", 0.0, null, 0, null, false);
            double rowAmount = row[1] instanceof Number n ? n.doubleValue() : 0.0;
            breakdown.add(new DashboardDto.CategoryAmount(cat, rowAmount));
            if (row[0] instanceof Category c) spentByCategoryId.put(c.getId(), rowAmount);

        }

        // Budget status
        List<DashboardDto.BudgetLine> budgets = new ArrayList<>();
        for (Category cat : categoryRepository.findByUserAndTypeOrderByNameAsc(user, TransactionType.EXPENSE)) {
            if (cat.hasBudget()) {
                double spent  = spentByCategoryId.getOrDefault(cat.getId(), 0.0);
                double budget = cat.getMonthlyBudget().doubleValue();
                double ratio  = budget > 0 ? spent / budget : 0.0;
                budgets.add(new DashboardDto.BudgetLine(CategoryDto.from(cat), spent, budget, ratio));
            }
        }

        List<AccountDto> accountDtos = accounts.stream()
                .map(a -> AccountDto.from(a, accountService.computeBalance(a, user)))
                .toList();

        List<TransactionDto> recent = transactionRepository
                .findRecentByUser(user, PageRequest.of(0, 5))
                .stream().map(TransactionDto::from).toList();

        return new DashboardDto(totalBalance, monthIncome, monthExpense, savingsRate,
                trend, breakdown, budgets, accountDtos, recent);
    }

    @Cacheable(value = CACHE_MONTHLY_REPORT, key = "#user.id + ':' + #year + ':' + #month")
    @Transactional(readOnly = true)
    public MonthlyReportDto getMonthlyReport(User user, int year, int month) {
        // Use the batch category--id query for income and expense sums
        LocalDate monthStart = LocalDate.of(year, month, 1);

        Map<String, double[]> monthlyMap = buildMonthlyMap(
            transactionRepository.sumByYearMonthAndType(user, monthStart));
        String key = year + "-" + month;
        double[] row = monthlyMap.getOrDefault(key, new double[]{0, 0});

        double income = row[0];
        double expense = row[1];
        double net = income - expense;
        double savingsRate = income > 0 ? Math.max(0, net / income * 100) : 0;

        // Full category breakdown ordered by spend DESC - no size cap, frontend decides how many to show.
        List<MonthlyReportDto.CategoryAmount> categorySpend = new ArrayList<>();
        for (Object[] r : transactionRepository.findCategoryBreakdown(user, year, month)) {
            CategoryDto cat = r[0] instanceof Category c
                ? CategoryDto.from(c)
                : new CategoryDto("uncategorized", "Uncategorized", TransactionType.EXPENSE, "#9ca3af", "📦", 0.0, null, 0, null, false);
            double amt = r[1] instanceof Number n ? n.doubleValue() : 0.0;
            categorySpend.add(new MonthlyReportDto.CategoryAmount(cat, amt));
        }

        List<Transaction> largest = transactionRepository.findLargestExpenseInMonth(user, year, month, PageRequest.of(0, 1));
        TransactionDto largestDto = largest.isEmpty() ? null : TransactionDto.from(largest.get(0));

        return new MonthlyReportDto(year, month, income, expense, net, savingsRate, categorySpend, largestDto);
    }

}
