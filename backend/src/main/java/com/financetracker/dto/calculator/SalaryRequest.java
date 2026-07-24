package com.financetracker.dto.calculator;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SalaryRequest(

    @Positive @DecimalMax("1000000000000") double amount,
    @NotBlank String timeFrame,

    // 168 hours per week;;; a near 0 value would blow up the 
    // per hour division to values (Fails JSON serialization)
    @DecimalMin("0.5") @DecimalMax("168") double hoursPerWeek
) {}
