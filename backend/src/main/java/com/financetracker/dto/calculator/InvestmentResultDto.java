package com.financetracker.dto.calculator;

import java.util.List;

public record InvestmentResultDto(

    double futureValue,
    double totalContributed,
    double interestEarned,
    double realValue,
    List<String> yearLabels,
    List<Double> balanceByYear,
    List<Double> contributionsByYear,
    List<Double> realByYear,
    List<YearRow> schedule
) {

    public record YearRow(
        int year,
        double startBalance,
        double contributions,
        double interest,
        double endBalance,
        double realValue
    ) {}

}
