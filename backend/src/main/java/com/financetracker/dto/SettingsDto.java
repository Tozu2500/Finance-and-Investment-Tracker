package com.financetracker.dto;

import java.util.Objects;

import com.financetracker.model.UserSettings;

public record SettingsDto(

    String currency,
    String language,
    boolean darkMode,
    double fontScale,
    String dateFormat,
    String weekStart,
    boolean compactMode,
    boolean budgetAlerts,
    boolean dailySummary,
    String defaultAccountId,
    boolean showCents,
    boolean notificationsEnabled
) {

    public static SettingsDto from(UserSettings s) {
        return new SettingsDto(
                s.getCurrency(),
                s.getLanguage(),
                Boolean.TRUE.equals(s.getDarkMode()),
                Objects.requireNonNullElse(s.getFontScale(), 1.0).doubleValue(),
                s.getDateFormat() != null ? s.getDateFormat() : "yyyy-MM-dd",
                s.getWeekStart() != null ? s.getWeekStart() : "Monday",
                Boolean.TRUE.equals(s.getCompactMode()),
                s.getBudgetAlerts() == null || Boolean.TRUE.equals(s.getBudgetAlerts()),
                Boolean.TRUE.equals(s.getDailySummary()),
                s.getDefaultAccountId(),
                s.getShowCents() == null || Boolean.TRUE.equals(s.getShowCents()),
                s.getNotificationsEnabled() == null || Boolean.TRUE.equals(s.getNotificationsEnabled())
        );
    }

}
