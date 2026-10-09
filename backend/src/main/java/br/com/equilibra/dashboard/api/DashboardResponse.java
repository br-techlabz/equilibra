package br.com.equilibra.dashboard.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardResponse(
    Period period,
    Summary summary,
    List<AccountBalance> accounts,
    List<RecentTransaction> recentTransactions,
    List<BalanceSeries> balanceEvolution,
    List<CategoryExpense> expenseByCategory,
    List<CategoryExpense> incomeByCategory
) {
    public record Period(Instant from, Instant to) {}
    public record BalanceSeries(String accountId, String accountName, List<BalancePoint> points) {}
    public record BalancePoint(Instant at, BigDecimal balance) {}
    public record CategoryExpense(String categoryName, BigDecimal amount) {}
    public record Summary(BigDecimal income, BigDecimal expense, BigDecimal net, BigDecimal netWorth) {}
    public record AccountBalance(String accountId, String name, String type, BigDecimal initialBalance, BigDecimal currentBalance, boolean active) {}
    public record RecentTransaction(String id, String type, String description, Instant occurredAt, BigDecimal amount, String sourceAccountId, String destinationAccountId, String status) {}
}
