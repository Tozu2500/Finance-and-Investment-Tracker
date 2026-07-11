package com.financetracker.dto;

import java.math.BigDecimal;

import com.financetracker.model.Account;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record AccountDto(
    String id,
    @NotBlank String name,
    String icon,
    @DecimalMin("-1000000000000") @DecimalMax("1000000000000") double openingBalance,
    double balance,
    String accountType,
    String colorHex,
    String description,
    String institution,
    @DecimalMax("1000000000000") Double creditLimit,
    boolean isArchived,
    int sortOrder,
    boolean isDefault
) {
    public static AccountDto from(Account a, double balance) {
        BigDecimal ob = a.getOpeningBalance();
        BigDecimal cl = a.getCreditLimit();
        return new AccountDto(
                a.getId(), a.getName(), a.getIcon(),
                ob != null ? ob.doubleValue() : 0.0,
                balance,
                a.getAccountType(), a.getColorHex(), a.getDescription(),
                a.getInstitution(),
                cl != null ? cl.doubleValue() : null,
                a.getIsArchived() != null && a.getIsArchived(),
                a.getSortOrder() != null ? a.getSortOrder() : 0,
                a.getIsDefault() != null && a.getIsDefault()
        );
    }

}
