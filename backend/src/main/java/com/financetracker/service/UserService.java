package com.financetracker.service;

import java.time.LocalDateTime;

import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financetracker.dto.UserDto;
import com.financetracker.dto.auth.AuthResponse;
import com.financetracker.dto.auth.LoginRequest;
import com.financetracker.dto.auth.RegisterRequest;
import com.financetracker.model.User;
import com.financetracker.model.UserSettings;
import com.financetracker.repository.UserRepository;
import com.financetracker.repository.UserSettingsRepository;
import com.financetracker.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final CategoryService categoryService;
    private final AccountService accountService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .build();
        userRepository.save(user);

        UserSettings settings = UserSettings.builder().user(user).build();
        settingsRepository.save(settings);

        categoryService.seedDefaults(user);
        accountService.seedDefaultIfMissing(user);

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, UserDto.from(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new BadRequestException("User account not found"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Self-heals any account that predates the account-seeding feature
        // (e.g. the demo user) so it isn't permanently stuck with zero accounts.
        accountService.seedDefaultIfMissing(user);
        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, UserDto.from(user));
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
