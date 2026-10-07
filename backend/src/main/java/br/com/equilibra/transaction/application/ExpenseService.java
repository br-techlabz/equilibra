package br.com.equilibra.transaction.application;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.api.CreateExpenseRequest;
import br.com.equilibra.transaction.api.ExpensePageResponse;
import br.com.equilibra.transaction.api.ExpenseResponse;
import br.com.equilibra.transaction.api.UpdateExpenseRequest;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private static final int MAX_PAGE_SIZE = 100;
    private final FinancialTransactionRepository transactions;
    private final AssetAccountRepository accounts;
    private final CategoryRepository categories;
    private final CurrentUser currentUser;
    private final TransactionTagService transactionTags;

    public ExpenseService(FinancialTransactionRepository transactions, AssetAccountRepository accounts,
                          CategoryRepository categories, CurrentUser currentUser, TransactionTagService transactionTags) {
        this.transactions = transactions;
        this.accounts = accounts;
        this.categories = categories;
        this.currentUser = currentUser;
        this.transactionTags = transactionTags;
    }

    @Transactional
    public ExpenseResponse create(CreateExpenseRequest request) {
        String owner = ownerId();
        AssetAccount account = eligibleAccount(request.accountId(), owner);
        Category category = eligibleCategory(request.categoryId(), owner);
        FinancialTransaction transaction = FinancialTransaction.expense(owner, request.description(), request.amount(), request.occurredAt(), category.getId(), account.getId(), request.notes());
        transactionTags.replace(transaction, request.tagIds());
        return ExpenseResponse.from(transactions.save(transaction));
    }

    @Transactional(readOnly = true)
    public ExpensePageResponse list(int page, int size, boolean includeCancelled) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("Invalid pagination parameters.");
        String owner = ownerId();
        Page<FinancialTransaction> result = transactions.findPageByOwnerAndTypeAndStatus(
            owner, TransactionType.EXPENSE, includeCancelled ? null : TransactionStatus.ACTIVE,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        return new ExpensePageResponse(result.getContent().stream().map(t -> ExpenseResponse.from(t, transactionTags.summaries(t))).toList(),
            result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(String id) { FinancialTransaction t = findExpense(id); return ExpenseResponse.from(t, transactionTags.summaries(t)); }

    @Transactional
    public ExpenseResponse update(String id, UpdateExpenseRequest request) {
        FinancialTransaction transaction = findExpense(id);
        if (transaction.getStatus() == TransactionStatus.CANCELLED) throw new ResourceConflictException("Cancelled expenses cannot be edited.");
        AssetAccount account = eligibleAccount(request.accountId(), ownerId());
        Category category = eligibleCategory(request.categoryId(), ownerId());
        transaction.changeDescription(request.description());
        transaction.changeAmount(request.amount());
        transaction.changeOccurredAt(request.occurredAt());
        transaction.changeNotes(request.notes());
        transaction.changeSourceAccount(account.getId());
        transaction.changeCategory(category.getId());
        if (request.tagIds() != null) transactionTags.replace(transaction, request.tagIds());
        try { return ExpenseResponse.from(transactions.save(transaction)); }
        catch (OptimisticLockingFailureException exception) { throw new ResourceConflictException("Expense was modified concurrently."); }
    }

    @Transactional
    public ExpenseResponse cancel(String id) {
        FinancialTransaction transaction = findExpense(id);
        try { transaction.cancel(); return ExpenseResponse.from(transactions.save(transaction)); }
        catch (IllegalStateException exception) { throw new ResourceConflictException("Expense is already cancelled."); }
    }

    private FinancialTransaction findExpense(String id) {
        validateUuid(id, "id");
        return transactions.findByIdAndOwnerIdAndType(id, ownerId(), TransactionType.EXPENSE)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found."));
    }

    private AssetAccount eligibleAccount(String id, String owner) {
        validateUuid(id, "accountId");
        AssetAccount account = accounts.findByIdAndOwnerId(id, owner)
            .orElseThrow(() -> new ResourceNotFoundException("Asset account not found."));
        if (!account.isActive()) throw new ResourceConflictException("Asset account is inactive.");
        return account;
    }

    private Category eligibleCategory(String id, String owner) {
        validateUuid(id, "categoryId");
        Category category = categories.findByIdAndOwnerId(id, owner)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
        if (!category.isActive()) throw new ResourceConflictException("Category is inactive.");
        if (category.getApplicability() != CategoryApplicability.EXPENSE && category.getApplicability() != CategoryApplicability.BOTH) {
            throw new ResourceConflictException("Category is not applicable to expenses.");
        }
        return category;
    }

    private String ownerId() { return currentUser.id().toString(); }
    private static void validateUuid(String value, String field) { try { UUID.fromString(value); } catch (Exception e) { throw new IllegalArgumentException(field + " must be a valid UUID.", e); } }
}
