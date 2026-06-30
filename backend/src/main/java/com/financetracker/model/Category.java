package com.financetracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_user_type", columnList = "user_id, type"),
    @Index(name = "idx_categories_user_active", columnList = "user_id, is_archived"),
    @Index(name = "idx_categories_parent", columnList = "parent_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String colorHex;
    private String icon;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "parent_id")
    private String parentCategoryId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isArchived = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public boolean hasBudget() {
        return monthlyBudget != null && monthlyBudget.compareTo(BigDecimal.ZERO) > 0;
    }
}
