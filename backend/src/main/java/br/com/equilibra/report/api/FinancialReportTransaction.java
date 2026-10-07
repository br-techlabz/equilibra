package br.com.equilibra.report.api;

import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record FinancialReportTransaction(
    String id,
    TransactionType type,
    TransactionStatus status,
    String description,
    Instant occurredAt,
    BigDecimal amount,
    String categoryId,
    String sourceAccountId,
    String destinationAccountId,
    String notes
) {}
