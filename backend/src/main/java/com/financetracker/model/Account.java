package com.financetracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "accounts", indexes = {
    @Index(name = "idx_accounts_user", columnList = "user_id"),
    @Index(name = "idx_accounts_user_active", columnList = "user_id, is_archived")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private String accountType = "CHECKING";

    private String colorHex;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String institution;

    @Column(precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isArchived = false;

    // User-defined display order (lower = first)
    @Builder.Default
    @Column(nullable = false, columnDefinition = "int not null default 0")
    private Integer sortOrder = 0;

    // True for the one account pre-selected when creating a new transaction.
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
