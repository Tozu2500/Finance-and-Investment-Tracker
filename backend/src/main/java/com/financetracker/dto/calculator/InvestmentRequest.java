package com.financetracker.dto.calculator;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

public record InvestmentRequest(

    @PositiveOrZero @DecimalMax("1000000000000") double principal,
    @PositiveOrZero @DecimalMax("1000000000") double monthlyContribution,
    @PositiveOrZero @DecimalMax("1000") double annualReturn,
    @PositiveOrZero @DecimalMax("1000") double contributionGrowth,

    // Unbound years would let a single request spin the projection loop
    @Min(1) @Max(200) int years,
    @PositiveOrZero @DecimalMax("1000") double inflation,
    @PositiveOrZero @DecimalMax("1000") double annualFees
) {}
