package com.financetracker.dto.auth;

import com.financetracker.dto.UserDto;

public record AuthResponse(

    String token, UserDto user) {}
