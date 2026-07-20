package com.financetracker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    
        @NotBlank @Size(max = 100) String name,
        @Email @NotBlank @Size(max = 254) String email,

    // BCrypt only hashes first 72 bytes, reject longer password (double standards, I know, we need to improvements upcoming)
    @NotBlank @Size(min = 6, max = 72) String password

) {}
