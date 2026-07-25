package com.financetracker.dto.report;

import java.util.List;

import com.financetracker.dto.AccountDto;
import com.financetracker.dto.CategoryDto;
import com.financetracker.dto.TransactionDto;

public record DashboardDto(

    double totalBalance,
    double monthIncome,
    double monthExpense,
    double savingsRate,
    List<MonthPoint> balanceTrend,
    List<CategoryAmount> spendingBreakdown,
    List<BudgetLine> budgets,
    List<AccountDto> accounts,
    List<TransactionDto> recentTransactions

) {

    public record MonthPoint(String month, double balance) {}
    
    public record CategoryAmount(CategoryDto category, double amount) {}

    public record BudgetLine(CategoryDto category, double spent, double budget, double ratio) {}
}
