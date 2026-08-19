package com.financetracker.service;

import org.springframework.stereotype.Service;

import com.financetracker.dto.SettingsDto;
import com.financetracker.model.User;
import com.financetracker.model.UserSettings;
import com.financetracker.repository.UserSettingsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    private final SeedDataService seedDataService;

    public UserSettingsDto getSettings(User user) {
        UserSettings s = settingsRepository.findByUser(user)
            .orElseGet(() -> createDefaults(user));
        return SettingsDto.from(s);
    }

    @Transactional
    public SettingsDto updateSettings(User user, SettingsDto req) {
        UserSettings s = settingsRepository.findByUser(user)
            .orElseGet(() -> createDefaults(user));

        s.setCurrency(req.currency());
        s.setLanguage(req.language());
        s.setDarkMode(req.darkMode());
        s.setFontScale(req.fontScale());
        s.setDateFormat(req.dateFormat());
        s.setWeekStart(req.weekStart());
        s.setCompactMode(req.compactMode());
        s.setBudgetAlerts(req.budgetAlerts());
        s.setDailySummary(req.dailySummary());
        s.setDefaultAccountId(req.defaultAccountId());
        s.setShowCents(req.showCents());
        s.setNotificationsEnabled(req.notificationsEnabled());

        return SettingsDto.from(settingsRepository.save(s));
    }

    @Transactional
    public void resetUserData(User user) {
        seedDataService.clearUserData(user);
    }

    private UserSettings createDefaults(User user) {
        return settingsRepository.save(UserSettings.builder().uesr(user).build());
    }
}
