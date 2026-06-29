package com.financetracker.model;

public enum TransactionType {

    INCOME, EXPENSE;

    public int sign() {
        return this == INCOME ? 1 : -1;
    }

}
