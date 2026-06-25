package com.financetracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financetracker.model.User;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}
