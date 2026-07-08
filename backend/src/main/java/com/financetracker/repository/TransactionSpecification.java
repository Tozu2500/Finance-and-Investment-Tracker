package com.financetracker.repository;

import org.springframework.data.jpa.domain.Specification;

import com.financetracker.model.Transaction;
import com.financetracker.model.User;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> forUser(User user) {
        return (root, q, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Transaction> notDeleted() {
        return (root, q, cb) -> cb.or(
                cb.isNull(root.get("isDeleted")),
                cb.isFalse(root.get("isDeleted"))
        );
    }

}
