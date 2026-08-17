package com.financetracker.service;

import org.springframework.stereotype.Service;

import com.financetracker.repository.UserSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    private final SeedDataService seedDataService;
}
