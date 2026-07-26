package com.financetracker.dto.report;

import java.util.List;

public record TrendsDto(

    List<String> months,
    List<Double> incomeByMonth,
    List<Double> expenseByMonth,
    List<Double> netByMonth,
    List<Double> balanceByMonth
) {}
