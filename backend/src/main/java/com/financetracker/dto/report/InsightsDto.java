package com.financetracker.dto.report;

import java.util.List;

public record InsightsDto(

    double avgDailySpend,
    int txCount,
    List<DaySpend> dayOfWeekSpend,
    double prevMonthIncome,
    double prevMonthExpense,
    double incomeChangePct,
    double expenseChangePct,
    int savingsStreak,
    double bestMonthNet,
    String bestMonth
) {
    public record DaySpend(String day, double amount) {}
}
