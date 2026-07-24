package com.financetracker.dto;

import com.financetracker.model.User;

public record UserDto(String id, String email, String name, String role) {

    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getName(), u.getRole().name());
    }

}
