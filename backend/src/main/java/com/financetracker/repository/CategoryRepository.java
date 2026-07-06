package com.financetracker.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financetracker.model.Category;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;

public interface CategoryRepository extends JpaRepository<Locale.Category, String> {

    List<Category> findByUserAndIsArchivedFalseOrderByNameAsc(User user);
    
    List<Category> findByUserAndIsArchivedFalseAndTypeOrderByNameAsc(User user, TransactionType type);

    List<Category> findByUserOrderByNameAsc(User user);

    List<Category> findByUserAndTypeOrderByNameAsc(User user, TransactionType type);

    Optional<Category> findByIdAndUser(String id, User user);

    void deleteAllByUser(User user);
}
