package com.financetracker.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.financetracker.model.RecurType;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;

import jakarta.persistence.criteria.JoinType;

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

    public static Specification<Transaction> withType(TransactionType type) {
        return (root, q, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> withCategoryId(String categoryId) {
        return (root, q, cb) -> categoryId == null ? cb.conjunction()
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> withAccountId(String accountId) {
        return (root, q, cb) -> accountId == null ? cb.conjunction()
                : cb.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> afterDate(LocalDate from) {
        return (root, q, cb) -> from == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    public static Specification<Transaction> beforeDate(LocalDate to) {
        return (root, q, cb) -> to == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("date"), to);
    }

    public static Specification<Transaction> noteContains(String search) {
        return (root, q, cb) -> {
            if (search == null || search.isBlank()) return cb.conjunction();
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(cb.coalesce(root.<String>get("note"), "")), pattern),
                cb.like(cb.lower(cb.coalesce(root.<String>get("merchant"), "")), pattern)
            );
        };
    }

    public static Specification<Transaction> recurringOnly(boolean recurring) {
        return (root, q, cb) -> recurring
            ? cb.notEqual(root.get("recur"), RecurType.NONE)
            : cb.conjunction();
    }

    public static Specification<Transaction> withFetches() {
        return (root, q, cb) -> {
            if (q.getResultType() != Long.class && q.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
                root.fetch("account", JoinType.LEFT);
            }

            return cb.conjunction();
        };
    }

}
