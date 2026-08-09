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

import org.apache.coyote.BadRequestException;

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

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true),
        @CacheEvict(value = CACHE_TRENDS, key = "#user.id"),
        @CacheEvict(value = CACHE_INSIGHTS, key = "#user.id")
    })
    @Transactional
    public void deleteTransaction(String id, User user) {
        Transaction tx = findOrThrow(id, user);
        tx.setIsDeleted(true);
        tx.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public String exportCsv(User user) {
        List<Transaction> all = transactionRepository.findByUserWithRelations(user);
        StringBuilder sb = new StringBuilder("date,amount,currency,type,category,account,merchant,location,note,recur\n");
        
        for (Transaction t : all) {
            sb.append(t.getDate()).append(',')
                .append(t.getAmount() != null ? t.getAmount().toPlainString() : "0").append(',')
                .append(t.getCurrency() != null ? t.getCurrency() : "USD").append(',')
                .append(t.getType()).append(',')
                .append(t.getCategory() != null ? escape(t.getCategory().getName()) : "").append(',')
                .append(t.getAccount() != null ? escape(t.getAccount().getName()) : "").append(',')
                .append(escape(t.getMerchant() != null ? t.getMerchant() : "")).append(',')
                .append(escape(t.getLocation() != null ? t.getLocation() : "")).append(',')
                .append(escape(t.getNote() != null ? t.getNote() : "")).append(',')
                .append(t.getRecur()).append('\n');
        }

        return sb.toString();
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id"),
        @CacheEvict(value = CACHE_MONTHLY_REPORT, allEntries = true),
        @CacheEvict(value = CACHE_TRENDS, key = "#user.id"),
        @CacheEvict(value = CACHE_INSIGHTS, key = "#user.id")
    })
    @Transactional
    public int importCsv(MultipartFile file, User user) {
        Map<String, Category> catByName = categoryRepository.findByUserOrderByNameAsc(user)
            .stream().collect(Collectors.toMap(c -> c.getName().toLowerCase(), c -> c, (a, b) -> a));

        Map<String, Account> accByName = accountRepository.findByUserOrderBySortOrderAscNameAsc(user)
            .stream().collect(Collectors.toMap(a -> a.getName().toLowerCase(), a -> a, (a, b) -> a));

        int count = 0;
        int skipped = 0;
        final int maxRows = 20_000;

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();  // Skipping the header

            while ((line = reader.readLine()) != null) {
                if (count >= maxRows) {
                    throw new BadRequestException("CSV import limited to " + maxRows + " rows per file");
                }

                String[] parts = splitCsvLine(line);
                if (parts.length < 3) { skipped++; continue; }

                try {
                    // CSV column order: date,amount,currency,type,category,account,merchant,location,note,recur
                    Transaction tx = Transaction.builder()
                        .date(LocalDate.parse(parts[0].trim()))
                        .amount(new BigDecimal(parts[1].trim()).abs())
                        .type(TransactionType.valueOf(parts[3].trim().toUpperCase()))
                        .merchant(parts.length > 6 ? unquote(parts[6]) : null)
                        .location(parts.length > 7 ? unquote(parts[7]) : null)
                        .note(parts.length > 8 ? unquote(parts[8]) : "")
                        .recur(parts.length > 9 ? safeRecur(parts[9].trim()) : RecurType.NONE)
                        .user(user)
                        .build();

                    if (parts.length > 2 && !parts[2].trim().isEmpty())
                        tx.setCurrency(parts[2].trim());
                    if (parts.length > 4 && !parts[4].trim().isEmpty())
                        tx.setCategory(catByName.get(unquote(parts[4]).toLowerCase()));
                    if (parts.length > 5 && !parts[5].trim().isEmpty())
                        tx.setAccount(accByName.get(unquote(parts[5]).toLowerCase()));

                    transactionRepository.save(tx);
                    count++;
                } catch (Exception ex) {
                    skipped++;
                    log.warn("CSV Import skipped row: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse CSV: " + e.getMessage());
        }

        if (skipped > 0) log.warn("CSV import: " + skipped + " row(s) skipped due to parse errors.");
        return count;
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder cur = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }

        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);

        return s.replace("\"\"", "\"");
    }

    private Transaction findOrThrow(String id, User user) {
        Transaction tx = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (Boolean.TRUE.equals(tx.getIsDeleted())) {
            throw new ResourceNotFoundException("Transaction not found: " + id);
        }

        return tx;
    }

    private Transaction buildTransaction(TransactionDto req, User user) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        applyFields(tx, req, user);

        return tx;
    }

    private void applyFields(Transaction tx, TransactionDto req, User user) {
        tx.setDate(req.date());
        tx.setAmount(BigDecimal.valueOf(Math.abs(req.amount())));
        tx.setType(req.type());
        tx.setNote(req.note());
        tx.setMerchant(req.merchant());
        tx.setLocation(req.location());

        if (req.currency() != null && !req.currency().isBlank()) tx.setCurrency(req.currency());
        tx.setRecur(req.recur() != null ? req.recur() : RecurType.NONE);

        if (req.transferId() != null) tx.setTransferId(req.transferId());

        if (req.category() != null && req.category().id() != null) {
            categoryRepository.findByIdAndUser(req.category().id(), user).ifPresent(tx::setCategory);
        } else {
            tx.setCategory(null);
        }

        if (req.account() != null && req.account().id() != null) {
            accountRepository.findByIdAndUser(req.account().id(), user).ifPresent(tx::setAccount);
        } else {
            tx.setAccount(null);
        }

        if (req.tagIds() != null) {
            Set<Tag> resolved = new HashSet<>(tagRepository.findAllByIdInAndUser(req.tagIds(), user));
            tx.setTags(resolved);
        }
    }

    private static String escape(String s) {
        // Neutralise spreadsheet formula injection: a leading =, +, -, @, tab
        // or CR makes Excel/Sheets evaluate the cell as a formula.
        if (!s.isEmpty() && "=+-@\t\r".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static RecurType safeRecur(String s) {
        try { return RecurType.valueOf(s.toUpperCase()); } catch (Exception e) { return RecurType.NONE; }
    }
}
