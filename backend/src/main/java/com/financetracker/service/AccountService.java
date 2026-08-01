package com.financetracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financetracker.dto.AccountDto;
import com.financetracker.model.Account;
import com.financetracker.model.User;
import com.financetracker.repository.AccountRepository;
import com.financetracker.repository.RecurringRuleRepository;
import com.financetracker.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringRuleRepository recurringRuleRepository;

    /**
     * Every transaction requires an account, so a user with zero accounts can
     * never add one — the add-transaction forms have no account to offer and
     * stay permanently disabled. Called on registration and on every login so
     * it also self-heals existing accounts (e.g. the seeded demo user) that
     * predate this method.
     */
    @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public void seedDefaultIfMissing(User user) {
        if (!accountRepository.findByUserOrderBySortOrderAscNameAsc(user).isEmpty()) {
            return;
        }

        Account account = Account.builder()
            .name("Cash")
            .icon("💵")
            .accountType("CASH")
            .openingBalance(BigDecimal.ZERO)
            .colorHex("#10b981")
            .isDefault(true)
            .sortOrder(0)
            .user(user)
            .build();
        accountRepository.save(account);
    }

    public List<AccountDto> getAccounts(User user, boolean includeArchived) {
        List<Account> accounts = includeArchived
                ? accountRepository.findByUserOrderBySortOrderAscNameAsc(user)
                : accountRepository.findByUserAndIsArchivedFalseOrderBySortOrderAscNameAsc(user);
        return accounts.stream()
            .map(a -> AccountDto.from(a, computeBalance(a, user)))
            .toList();
    }

    public AccountDto getAccount(String id, User user) {
        Account a = findOrThrow(id, user);
        return AccountDto.from(a, computeBalance(a, user));
    }

    @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public AccountDto createAccount(AccountDto req, User user) {
        Account account = Account.builder()
            .name(req.name())
            .icon(req.icon())
            .openingBalance(BigDecimal.valueOf(req.openingBalance()))
            .accountType(req.accountType() != null ? req.accountType() : "CHECKING")
            .colorHex(req.colorHex())
            .description(req.description())
            .institution(req.institution())
            .creditLimit(req.creditLimit() != null ? BigDecimal.valueOf(req.creditLimit()) : null)
            .sortOrder(req.sortOrder())
            .isDefault(req.isDefault())
            .user(user)
            .build();

        if (req.id() != null && !req.id().isBlank()) {
            // Client-generated UUIDs (desktop sync) are honored only when unused;;;
            // otherwise save() would merge into and overwrite an existing row
            if (accountRepository.existsById(req.id())) {
                throw new BadRequestException("Account id already exists: " + req.id());
            }

            account.setId(req.id());
        }

        // If this is the first account, we make it the default automatically
        if (accountRepository.findByUserAndIsDefaultTrue(user).isEmpty()) {
            account.setIsDefault(true);
        } else if (req.isDefault()) {
            accountRepository.clearDefaultForUser(user);
        }

        Account saved = accountRepository.save(account);
        return AccountDto.from(saved, computeBalance(saved, user));
    }

    @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public AccountDto updateAccount(String id, AccountDto req, User user) {
        Account account = findOrThrow(id, user);

        account.setName(req.name());
        account.setIcon(req.icon());
        account.setOpeningBalance(BigDecimal.valueOf(req.openingBalance()));

        if (req.accountType() != null) {
            account.setAccountType(req.accountType());
        }

        account.setColorHex(req.colorHex());
        account.setDescription(req.description());
        account.setInstitution(req.institution());

        account.setCreditLimit(req.creditLimit() != null ? BigDecimal.valueOf(req.creditLimit()) : null);
        account.setSortOrder(req.sortOrder());

        if (req.isDefault() && !Boolean.TRUE.equals(account.getIsDefault())) {
            accountRepository.clearDefaultForUser(user);
            account.setIsDefault(true);
        }

        accountRepository.save(account);
        return AccountDto.from(account, computeBalance(account, user));
    }

    @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public AccountDto setDefault(String id, User user) {
        accountRepository.clearDefaultForUser(user);
        Account account = findOrThrow(id, user);
        account.setIsDefault(true);
        accountRepository.save(account);
        return AccountDto.from(account, computeBalance(account, user));
    }

    @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public AccountDto archiveAccount(String id, boolean archived, User user) {
        Account account = findOrThrow(id, user);
        account.setisArchived(archived);
        accountRepository.save(account);
        return AccountDto.from(account, computeBalance(account, user));
    }

    @CacheEvict(value = CACHE_DASHBOARD, key = "#user.id")
    @Transactional
    public void deleteAccount(String id, User user) {
        Account account = findOrThrow(id, user);
        transactionRepository.nullifyAccountReferences(account.getId(), user);
        recurringRuleRepository.nullifyAccountReferences(account.getId(), user);
        accountRepository.deleteById(account.getId());
    }

    public double computeBalance(Account account, User user) {
        BigDecimal txSum = accountRepository.sumSignedAmountsForAccount(account, user);
        BigDecimal opening = account.getOpeningBalance() != null ? account.getOpeningBalance() : BigDecimal.ZERO;
        BigDecimal total = opening.add(txSum != null ? txSum : BigDecimal.ZERO);
        return total.doubleValue();
    }

    private Account findOrThrow(String id, User user) {
        return accountRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
    }
}
