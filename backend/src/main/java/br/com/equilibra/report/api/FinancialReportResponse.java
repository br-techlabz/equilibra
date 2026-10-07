package br.com.equilibra.report.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FinancialReportResponse(
    Period period,
    List<AccountSelection> accounts,
    BigDecimal openingBalance,
    BigDecimal incomeTotal,
    BigDecimal expenseTotal,
    BigDecimal financialResult,
    BigDecimal incomingTransferTotal,
    BigDecimal outgoingTransferTotal,
    BigDecimal internalTransferTotal,
    BigDecimal balanceChange,
    BigDecimal closingBalance,
    Details details
) {
    public record Period(Instant from, Instant to) {}
    public record AccountSelection(String id, String name, boolean active) {}
    public record Details(List<FinancialReportTransaction> content, int page, int size, long totalElements, int totalPages) {}
}
