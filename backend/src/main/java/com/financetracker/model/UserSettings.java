package com.financetracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default private String currency = "USD";
    @Builder.Default private String language = "EN";
    @Builder.Default private Boolean darkMode = false;
    @Builder.Default private Double fontScale = 1.0;
    @Builder.Default private String dateFormat = "YYYY-MM-DD";
    @Builder.Default private String weekStart = "Monday";
    @Builder.Default private Boolean compactMode = false;
    @Builder.Default private Boolean budgetAlerts = true;
    @Builder.Default private Boolean dailySummary = false;

}
