package com.financetracker.service;

import com.financetracker.dto.TransactionDto;
import com.financetracker.exception.BadRequestException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.model.*;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.CategoryRepository;
import com.financetracker.repository.TagRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;

import static com.financetracker.config.CacheConfig.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(User user, TransactionType type,
                                            String categoryId, String accountId,
                                            LocalDate from, LocalDate to,
                                            String search, boolean recurring,
                                            int page, int size) {
        
        Specification<Transaction> spec = Specification
                .where(TransactionSpecification.forUser(user))
                .and(TransactionSpecification.notDeleted())
                .and(TransactionSpecification.withType(type))
                .and(TransactionSpecification.withCategoryId(categoryId))
                .and(TransactionSpecification.withAccountId(accountId))
                .and(TransactionSpecification.afterDate(from))
                .and(TransactionSpecification.beforeDate(to))
                .and(TransactionSpecification.noteContains(search))
                .and(TransactionSpecification.recurringOnly(recurring))
                .and(TransactionSpecification.withFetches());

        PageRequest pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("id").descending()));
        Page<Transaction> txPage = transactionRepository.findAll(spec, pageable);
        List<TransactionDto> dtos = txPage.getContent().stream()
                .map(TransactionDto::from).toList();

        return new PageImpl<>(dtos, pageable, txPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TransactionDto getById(String id, User user) {
        return TransactionDto.from(findOrThrow(id, user));
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true),
        @CacheEvict(value = CACHE_TRENDS, key = "#user.id"),
        @CacheEvict(value = CACHE_INSIGHTS, key = "#user.id")
    })
    @Transactional
    public TransactionDto createTransaction(TransactionDto req, User user) {
        Transaction tx = buildTransaction(req, user);
        return TransactionDto.from(transactionRepository.save(tx));
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true),
        @CacheEvict(value = CACHE_TRENDS, key = "#user.id"),
        @CacheEvict(value = CACHE_INSIGHTS, key = "#user.id")
    })
    @Transactional
    public TransactionDto updateTransaction(String id, TransactionDto req, User user) {
        Transaction tx = findOrThrow(id, user);
        applyFields(tx, req, user);
        return TransactionDto.from(transactionRepository.save(tx));
    }
}
