package com.financetracker.dto;

import java.math.BigDecimal;

import com.financetracker.model.BudgetPeriod;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetPeriodDto(
        String id,
        @NotNull String categoryId,
        CategoryDto category,
        @NotNull @Min(2000) @Max(2200) int year,
        @NotNull @Min(1) @Max(12) int month,
        @Positive @DecimalMax("1000000000000") double amount,
        String note
) {
    public static BudgetPeriodDto from(BudgetPeriod bp) {
        BigDecimal amt = bp.getAmount();
        return new BudgetPeriodDto(
                bp.getId(),
                bp.getCategory().getId(),
                CategoryDto.from(bp.getCategory()),
                bp.getYear(),
                bp.getMonth(),
                amt != null ? amt.doubleValue() : 0.0,
                bp.getNote()
        );
    }
}
