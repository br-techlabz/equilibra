package br.com.equilibra.account.application;

import br.com.equilibra.account.api.ReconcileAccountRequest;
import br.com.equilibra.account.api.ReconciliationResultResponse;
import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class BalanceReconciliationService {

    private static final int MONEY_SCALE = 2;
    private static final String ADJUSTMENT_CATEGORY_NAME = "Ajuste de Saldo";
    private static final String ADJUSTMENT_DESCRIPTION = "Ajuste de Saldo";

    private final AssetAccountRepository accounts;
    private final FinancialTransactionRepository transactions;
    private final CategoryRepository categories;
    private final CurrentUser currentUser;

    public BalanceReconciliationService(
        AssetAccountRepository accounts,
        FinancialTransactionRepository transactions,
        CategoryRepository categories,
        CurrentUser currentUser
    ) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.categories = categories;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalanceAt(String accountId, String ownerId, LocalDate date) {
        AssetAccount account = accounts.findByIdAndOwnerId(accountId, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset account not found."));

        // Use Brazil timezone (America/Sao_Paulo) for end-of-day cutoff
        // MySQL TIMESTAMP(6) supports microsecond precision
        ZoneId brazilZone = ZoneId.of("America/Sao_Paulo");
        Instant cutoff = date.atTime(23, 59, 59, 999999000).atZone(brazilZone).toInstant();

        BigDecimal balance = account.getInitialBalance();
        List<FinancialTransaction> activeTransactions = transactions
            .findAllByOwnerIdAndStatusOrderByOccurredAtAsc(ownerId, TransactionStatus.ACTIVE);

        for (FinancialTransaction tx : activeTransactions) {
            if (tx.getOccurredAt().isAfter(cutoff)) {
                continue;
            }
            if (tx.getSourceAccountId() != null && tx.getSourceAccountId().equals(accountId)) {
                balance = balance.subtract(tx.getAmount());
            }
            if (tx.getDestinationAccountId() != null && tx.getDestinationAccountId().equals(accountId)) {
                balance = balance.add(tx.getAmount());
            }
        }

        return balance.setScale(MONEY_SCALE);
    }

    @Transactional
    public ReconciliationResultResponse reconcile(String accountId, ReconcileAccountRequest request) {
        String ownerId = currentUser.id().toString();
        validateUuid(accountId, "accountId");

        AssetAccount account = accounts.findByIdAndOwnerId(accountId, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Asset account not found."));

        if (!account.isActive()) {
            throw new ResourceConflictException("Asset account is inactive.");
        }

        BigDecimal informedBalance = validateMoney(request.informedBalance(), "informedBalance");
        LocalDate date = request.reconciliationDate();
        LocalTime time = LocalTime.parse(request.reconciliationTime());
        ZoneId brazilZone = ZoneId.of("America/Sao_Paulo");

        if (date.isAfter(LocalDate.now(brazilZone))) {
            throw new IllegalArgumentException("Reconciliation date cannot be in the future.");
        }

        BigDecimal calculatedBalance = calculateBalanceAt(accountId, ownerId, date);
        BigDecimal difference = informedBalance.subtract(calculatedBalance).setScale(MONEY_SCALE, java.math.RoundingMode.UNNECESSARY);

        String adjustmentType = "NONE";
        String createdTransactionId = null;

        if (difference.signum() != 0) {
            Category category = getOrCreateAdjustmentCategory(ownerId);
            // Use the specified time in Brazil timezone
            // MySQL TIMESTAMP(6) supports microsecond precision
            Instant occurredAt = date.atTime(time).atZone(brazilZone).toInstant();

            FinancialTransaction tx;
            if (difference.signum() > 0) {
                // Positivo: Informado > Calculado -> Criar Receita
                tx = FinancialTransaction.income(
                    ownerId,
                    ADJUSTMENT_DESCRIPTION,
                    difference.abs(),
                    occurredAt,
                    category.getId(),
                    accountId,
                    "Reconciliação automática de saldo"
                );
                adjustmentType = "INCOME";
            } else {
                // Negativo: Informado < Calculado -> Criar Despesa
                tx = FinancialTransaction.expense(
                    ownerId,
                    ADJUSTMENT_DESCRIPTION,
                    difference.abs(),
                    occurredAt,
                    category.getId(),
                    accountId,
                    "Reconciliação automática de saldo"
                );
                adjustmentType = "EXPENSE";
            }

            FinancialTransaction saved = transactions.save(tx);
            createdTransactionId = saved.getId();
        }

        return new ReconciliationResultResponse(
            accountId,
            date,
            calculatedBalance,
            informedBalance,
            difference,
            adjustmentType,
            createdTransactionId
        );
    }

    private Category getOrCreateAdjustmentCategory(String ownerId) {
        String normalized = Category.normalizeName(ADJUSTMENT_CATEGORY_NAME);
        List<Category> userCategories = categories.findAllByOwnerIdOrderByNameAsc(ownerId);
        for (Category cat : userCategories) {
            if (cat.getNormalizedName().equals(normalized)) {
                if (!cat.isActive()) {
                    cat.activate();
                    categories.save(cat);
                }
                return cat;
            }
        }

        Category newCategory = new Category(ownerId, ADJUSTMENT_CATEGORY_NAME, CategoryApplicability.BOTH);
        return categories.save(newCategory);
    }

    private static BigDecimal validateMoney(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        if (value.scale() > MONEY_SCALE) {
            throw new IllegalArgumentException(field + " must have at most 2 decimal places");
        }
        return value.setScale(MONEY_SCALE);
    }

    private static void validateUuid(String value, String field) {
        try {
            UUID.fromString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(field + " must be a valid UUID.", e);
        }
    }
}
