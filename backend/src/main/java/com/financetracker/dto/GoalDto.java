package com.financetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.financetracker.model.Goal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record GoalDto(

    String id,
    @NotBlank String name,
    @Positive @DecimalMax("1000000000000") double targetAmount,
    @PositiveOrZero @DecimalMax("1000000000000") double savedAmount,
    LocalDate deadline,
    String icon,
    String colorHex,
    String description
) {

    public static GoalDto from(Goal g) {
        BigDecimal ta = g.getTargetAmount();
        BigDecimal sa = g.getSavedAmount();
        return new GoalDto(g.getId(), g.getName(),
                ta != null ? ta.doubleValue() : 0.0,
                sa != null ? sa.doubleValue() : 0.0,
                g.getDeadline(), g.getIcon(), g.getColorHex(), g.getDescription());
    }

}
