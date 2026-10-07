package br.com.equilibra.transaction.api;

import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseResponse(
    String id,
    String description,
    Instant occurredAt,
    String accountId,
    BigDecimal amount,
    String categoryId,
    String notes,
    TransactionStatus status,
    Instant createdAt,
    Instant updatedAt,
    java.util.List<br.com.equilibra.transaction.domain.TagSummary> tags
) {
    public static ExpenseResponse from(FinancialTransaction transaction) {
        return from(transaction, java.util.List.of());
    }
    public static ExpenseResponse from(FinancialTransaction transaction, java.util.List<br.com.equilibra.transaction.domain.TagSummary> tags) {
        return new ExpenseResponse(
            transaction.getId(), transaction.getDescription(), transaction.getOccurredAt(),
            transaction.getSourceAccountId(), transaction.getAmount(), transaction.getCategoryId(),
            transaction.getNotes(), transaction.getStatus(), transaction.getCreatedAt(), transaction.getUpdatedAt(), tags
        );
    }
}
