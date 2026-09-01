package com.financetracker.dto;

import java.math.BigDecimal;

import com.financetracker.model.Category;
import com.financetracker.model.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryDto(

    String id,
    @NotBlank String name,
    @NotNull TransactionType type,
    String colorHex,
    String icon,
    double monthlyBudget,
    String description,
    int sortOrder,
    String parentCategoryId,
    boolean isArchived,
    boolean excludeFromSpending
) {
    public static CategoryDto from(Category c) {
        BigDecimal mb = c.getMonthlyBudget();
        Integer so = c.getSortOrder();
        Boolean archived = c.getIsArchived();
        Boolean excluded = c.getExcludeFromSpending();

        return new CategoryDto(c.getId(), c.getName(), c.getType(),
                        c.getColorHex(), c.getIcon(),
                    mb != null ? mb.doubleValue() : 0.0,
                    c.getDescription(),
                    so != null ? so.intValue() : 0,
                    c.getParentCategoryId(),
                    archived != null && archived,
                    excluded != null && excluded);
    }

}
