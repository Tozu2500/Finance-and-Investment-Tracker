package com.financetracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.financetracker.repository.UserRepository;
import com.financetracker.service.CategoryService;
import com.financetracker.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component 
@RequiredArgsConstructor 
@Slf4j 
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;

    /**
     * The demo account has a well known password that gets re applied on
     * every startup -- that is a backdoor anywhere but local development, so
     * this step must be disabled in production via app.seed.demo-users=false
     */
    @Value("${app.seed.demo-users=true}")
    private boolean seedDemoUsers;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedDemoUsers) {
            log.info("Demo user seeding disabled (app.seed.demo-users=false)");
            return;
        }
        ensureAccountSafe("Demo User", "test@demo.com", "tester");
    }

    private void ensureAccountSafe(String name, String email, String password) {
        try {
            ensureAccount(name, email, password);
        } catch (Exception ex) {
            log.error("DataInitializer: could not seed '{}' - {}", email, ex.getMessage(), ex);
        }
    }

    /**
     * Creates the demo account on first run (empty — no sample data). On
     * subsequent backend restarts only the password is re-confirmed so that
     * user data accumulated during a session (via the web or desktop) is
     * not wiped.
     */
    private void ensureAccount(String name, String email, String password) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            user.setName(name);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            // No-op when categories already exist — only backfills an account
            // that was created outside the register() path (e.g. via seed.sql).
            categoryService.seedDefaults(user);
            log.info("Verified credentials for existing account: {}", email);
        }, () -> {
            userService.register(new RegisterRequest(name, email, password));
            log.info("Created account: {}", email);
        });
    }
}
