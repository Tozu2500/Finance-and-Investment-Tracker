package com.financetracker.dto;

import java.time.LocalDate;
import java.util.List;

import com.financetracker.model.RecurType;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionDto(

    String id,
    @NotNull LocalDate date,

    // Capped well below Double.MAX_VALUE so downstream sums (dashboard and reports)
    // cannot overflow to Infinity, which fails JSON serialization.
    @Positive @DecimalMax("1000000000000") double amount,
    @NotNull TransactionType type,
    CategoryDto category,
    AccountDto account,
    String note,
    String merchant,
    String location,
    String currency,
    RecurType recur,
    String transferId,
    List<TagDto> tags,
    List<String> tagIds  // Write-only: tag ids to associate on create/update

) {
    public static TransactionDto from(Transaction t, double accountBalance) {
        CategoryDto cat = t.getCategory() != null ? CategoryDto.from(t.getCategory()) : null;

        AccountDto acc = t.getAccount() != null
                ? AccountDto.from(t.getAccount(), accountBalance) : null;

        List<TagDto> tagList = t.getTags() != null
                ? t.getTags().stream().map(TagDto::from).toList()
                : List.of();

        double amt = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
        return new TransactionDto(t.getId(), t.getDate(), amt, t.getType(),
                    cat, acc, t.getNote(), t.getMerchant(), t.getLocation(), t.getCurrency(),
                    t.getRecur(), t.getTransferId(), tagList, null);
    }

    public static TransactionDto from(Transaction t) {
        return from(t, 0);
    }
} 
