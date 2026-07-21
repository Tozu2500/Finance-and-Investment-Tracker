package com.financetracker.dto;

import java.time.LocalDateTime;

import com.financetracker.model.GoalDeposit;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;

public record GoalDepositDto(

    String id,
    String goalId,
    @Positive @DecimalMax("1000000000000") double amount,
    String note,
    LocalDateTime createdAt
) {

    public static GoalDepositDto from(GoalDeposit d) {
        return new GoalDepositDto(
            d.getId(),
            d.getGoal().getId(),
            d.getAmount().doubleValue(),
            d.getNote(),
            d.getCreatedAt()
        );
    }

}
