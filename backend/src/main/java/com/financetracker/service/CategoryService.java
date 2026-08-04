package com.financetracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.financetracker.dto.CategoryDto;
import com.financetracker.model.Category;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TransactionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<CategoryDto> getCategories(User user, TransactionType type, boolean includeArchived) {
        List<Category> cats;

        if (includeArchived) {
            cats = type != null
                ? categoryRepository.findByUserAndTypeOrderByNameAsc(user, type)
                : categoryRepository.findByUserOrderByNameAsc(user);
        } else {
            cats = type != null
                ? categoryRepository.findByUserAndIsArchivedFalseAndTypeOrderByNameAsc(user, type)
                : categoryRepository.findByUserAndIsArchivedFalseOrderByNameAsc(user);
        }

        return cats.stream().map(CategoryDto::from).toList();
    }
    
    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true)
    })
    @Transactional
    public CategoryDto createCategory(CategoryDto req, User user) {
        Category cat = Category.builder()
            .name(req.name())
            .type(req.type())
            .colorHex(req.colorHex())
            .icon(req.icon())
            .monthlyBudget(BigDecimal.valueOf(req.monthlyBudget()))
            .description(req.description())
            .sortOrder(req.sortOrder())
            .user(user)
            .build();
        cat.setParentCategoryId(req.parentCategoryId());

        return CategoryDto.from(categoryRepository.save(cat));
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true)
    })
    @Transactional
    public CategoryDto updateCategory(String id, CategoryDto req, User user) {
        Category cat = findOrThrow(id, user);
        cat.setName(req.name());
        cat.setType(req.type());
        cat.setColorHex(req.colorHex());
        cat.setIcon(req.icon());
        cat.setMonthlyBudget(BigDecimal.valueOf(req.monthlyBudget()));
        cat.setDescription(req.description());
        cat.setSortOrder(req.sortOrder());
        cat.setParentCategoryId(req.parentCategoryId());
        
        return CategoryDto.from(categoryRepository.save(cat));
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true)
    })
    @Transactional
    public CategoryDto archiveCategory(String id, boolean archived, User user) {
        Category cat = findOrThrow(id, user);
        cat.setIsArchived(archived);
        return CategoryDto.from(categoryRepository.save(cat));
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true)
    })
    @Transactional
    public void deleteCategory(String id, User user) {
        Category cat = findOrThrow(id, user);
        transactionRepository.nullifyCategoryReferences(cat.getId(), user);
        categoryRepository.deleteById(cat.getId());
    }

    private Category findOrThrow(String id, User user) {
        return categoryRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    /**
     * Creates the starter category set for a new user so transactions can be
     * classified immediately after registration. Skips silently if the user
     * already has categories (safe to call more than once).
     */
    @Transactional
    public void seedDefaults(User user) {
        if (!categoryRepository.findByUserOrderByNameAsc(user).isEmpty()) return;

        record Def(String name, TransactionType type, String icon, String color) {}

        List<Def> defaults = List.of(
            new Def("Salary",        TransactionType.INCOME,  "💼", "#10b981"),
                new Def("Freelance",     TransactionType.INCOME,  "💻", "#6366f1"),
                new Def("Investments",   TransactionType.INCOME,  "📈", "#f59e0b"),
                new Def("Rent",          TransactionType.EXPENSE, "🏠", "#ef4444"),
                new Def("Groceries",     TransactionType.EXPENSE, "🛒", "#f97316"),
                new Def("Transport",     TransactionType.EXPENSE, "🚗", "#8b5cf6"),
                new Def("Dining Out",    TransactionType.EXPENSE, "🍔", "#ec4899"),
                new Def("Entertainment", TransactionType.EXPENSE, "🎬", "#06b6d4"),
                new Def("Utilities",     TransactionType.EXPENSE, "⚡", "#64748b"),
                new Def("Health",        TransactionType.EXPENSE, "🏥", "#10b981"),
                new Def("Shopping",      TransactionType.EXPENSE, "🛍️", "#f59e0b"),
                new Def("Subscriptions", TransactionType.EXPENSE, "📱", "#6366f1")
        );

        int order = 0;

        for (Def d : defaults) {
            categoryRepository.save(Category.builder()
                .name(d.name())
                .type(d.type())
                .icon(d.icon())
                .colorHex(d.color())
                .monthlyBudget(BigDecimal.ZERO)
                .sortOrder(order++)
                .user(user)
                .build());
        }
    }
}
