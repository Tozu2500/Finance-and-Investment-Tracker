package com.financetracker.dto.report;

import java.util.List;

import com.financetracker.dto.CategoryDto;
import com.financetracker.dto.TransactionDto;

public record MonthlyReportDto(

    int year,
    int month,
    double income,
    double expense,
    double net,
    double savingsRate,
    List<CategoryAmount> topCategories,
    TransactionDto largestExpense
) {

    public record CategoryAmount(CategoryDto category, double amount) {}
}
