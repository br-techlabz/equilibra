package br.com.equilibra.account.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReconciliationResultResponse(
    String accountId,
    LocalDate reconciliationDate,
    BigDecimal calculatedBalance,
    BigDecimal informedBalance,
    BigDecimal difference,
    String adjustmentType,
    String createdTransactionId
) {
}