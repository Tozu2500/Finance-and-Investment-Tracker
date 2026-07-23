package com.financetracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.financetracker.model.RecurType;
import com.financetracker.model.RecurringRule;
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
    String note,
    String merchant,
    boolean isActive,
    LocalDateTime lastTriggeredAt,
    LocalDateTime createdAt
) {

    public static RecurringRuleDto from(RecurringRule r) {
        return new RecurringRuleDto(
            r.getId(),
            r.getName(),
            r.getAmount().doubleValue(),
            r.getCurrency(),
            r.getType(),
            r.getFrequency(),
            r.getDayOfMonth(),
            r.getNextDueDate(),
            r.getCategory() != null ? CategoryDto.from(r.getCategory()) : null,
            r.getAccount() != null ? AccountDto.from(r.getAccount(), 0) : null,
            r.getNote(),
            r.getMerchant(),
            Boolean.TRUE.equals(r.getIsActive()),
            r.getLastTriggeredAt(),
            r.getCreatedAt()
        );
    }
}
