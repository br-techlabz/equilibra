package br.com.equilibra.transaction.application;

import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.api.TransactionHistoryPageResponse;
import br.com.equilibra.transaction.api.TransactionHistoryResponse;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionHistoryService {
    private final FinancialTransactionRepository repository;
    private final AssetAccountRepository accounts;
    private final CategoryRepository categories;
    private final CurrentUser current;

    public TransactionHistoryService(FinancialTransactionRepository repository, AssetAccountRepository accounts,
                                     CategoryRepository categories, CurrentUser current) {
        this.repository = repository;
        this.accounts = accounts;
        this.categories = categories;
        this.current = current;
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPageResponse list(int page, int size, TransactionType type, TransactionStatus status,
                                               Instant from, Instant to, String accountId, String categoryId) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination parameters.");
        if (from != null && to != null && !from.isBefore(to)) throw new IllegalArgumentException("from must be before to");
        validateOptionalUuid(accountId, "accountId");
        validateOptionalUuid(categoryId, "categoryId");
        String owner = current.id().toString();
        if (accountId != null && accounts.findByIdAndOwnerId(accountId, owner).isEmpty()) {
            throw new ResourceNotFoundException("Asset account not found.");
        }
        if (categoryId != null && categories.findByIdAndOwnerId(categoryId, owner).isEmpty()) {
            throw new ResourceNotFoundException("Category not found.");
        }
        Page<FinancialTransaction> result = repository.findHistory(owner, type, status, from, to, accountId, categoryId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        return new TransactionHistoryPageResponse(result.getContent().stream().map(TransactionHistoryResponse::from).toList(),
            result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }


    private static void validateOptionalUuid(String value, String field) {
        if (value != null) {
            try { UUID.fromString(value); }
            catch (IllegalArgumentException exception) { throw new IllegalArgumentException(field + " must be a valid UUID.", exception); }
        }
    }
}
