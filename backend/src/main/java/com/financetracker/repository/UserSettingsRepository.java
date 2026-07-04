package com.financetracker.repository;

import com.financetracker.model.User;
import com.financetracker.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
    
    Optional<UserSettings> findByUser(User user);
}
