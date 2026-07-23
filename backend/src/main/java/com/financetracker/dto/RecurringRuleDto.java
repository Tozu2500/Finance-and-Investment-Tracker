package com.financetracker.dto;

import java.time.LocalDate;

import com.financetracker.model.RecurType;
import com.financetracker.model.TransactionType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecurringRuleDto(

    String id,
    @NotBlank String name,
    @Positive @DecimalMax("1000000000000") double amount,
    String currency,
    @NotNull TransactionType type,
    @NotNull RecurType frequency,
    Integer dayOfMonth,
    LocalDate nextDueDate,
    CategoryDto category,
    AccountDto account,
    

) {

}
