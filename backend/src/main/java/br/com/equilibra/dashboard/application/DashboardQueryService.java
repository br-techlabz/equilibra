package br.com.equilibra.dashboard.application;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.dashboard.api.DashboardResponse;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardQueryService {
    private final AssetAccountRepository accounts;
    private final CategoryRepository categories;
    private final FinancialTransactionRepository transactions;
    private final CurrentUser currentUser;
    public DashboardQueryService(AssetAccountRepository accounts, FinancialTransactionRepository transactions, CurrentUser currentUser, CategoryRepository categories){this.accounts=accounts;this.transactions=transactions;this.currentUser=currentUser;this.categories=categories;}
    @Transactional(readOnly=true)
    public DashboardResponse query(Instant from, Instant to){
        if(from==null||to==null||!from.isBefore(to))throw new IllegalArgumentException("from must be before to");
        String owner=currentUser.id().toString();
        List<AssetAccount> accountList=accounts.findAllByOwnerIdAndActiveTrueOrderByNameAsc(owner);
        List<FinancialTransaction> active=transactions.findAllByOwnerIdAndStatusAndOccurredAtBetweenOrderByOccurredAtDesc(owner,TransactionStatus.ACTIVE,from,to);
        List<FinancialTransaction> before=transactions.findAllByOwnerIdAndStatusAndOccurredAtBeforeOrderByOccurredAtAsc(owner,TransactionStatus.ACTIVE,from);
        BigDecimal income=sum(active,TransactionType.INCOME),expense=sum(active,TransactionType.EXPENSE);
        List<FinancialTransaction> allActive = transactions.findAllByOwnerIdAndStatusOrderByOccurredAtDesc(owner, TransactionStatus.ACTIVE);
        List<FinancialTransaction> allRecent = allActive.stream().limit(10).toList();
        Map<String, BigDecimal> effects = new HashMap<>();
        allActive.forEach(transaction -> {
            if (transaction.getSourceAccountId() != null) {
                effects.merge(transaction.getSourceAccountId(), transaction.getAmount().negate(), BigDecimal::add);
            }
            if (transaction.getDestinationAccountId() != null) {
                effects.merge(transaction.getDestinationAccountId(), transaction.getAmount(), BigDecimal::add);
            }
        });
        List<DashboardResponse.AccountBalance> balances = accountList.stream()
            .map(a -> new DashboardResponse.AccountBalance(a.getId(), a.getName(), a.getType().name(),
                a.getInitialBalance(), a.getInitialBalance().add(effects.getOrDefault(a.getId(), BigDecimal.ZERO)), a.isActive()))
            .toList();
        BigDecimal netWorth=balances.stream().map(DashboardResponse.AccountBalance::currentBalance).reduce(BigDecimal.ZERO,BigDecimal::add);
        List<DashboardResponse.RecentTransaction> recent=allRecent.stream().map(t->new DashboardResponse.RecentTransaction(t.getId(),t.getType().name(),t.getDescription(),t.getOccurredAt(),t.getAmount(),t.getSourceAccountId(),t.getDestinationAccountId(),t.getStatus().name())).toList();
        Map<String, BigDecimal> opening = new HashMap<>();
        accountList.forEach(account -> opening.put(account.getId(), account.getInitialBalance()));
        before.forEach(transaction -> apply(opening, transaction));
        Map<String, List<DashboardResponse.BalancePoint>> points = new HashMap<>();
        LocalDate firstDay = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate lastDay = to.minusNanos(1).atZone(ZoneOffset.UTC).toLocalDate();
        for (AssetAccount account : accountList) { List<DashboardResponse.BalancePoint> values = new java.util.ArrayList<>(); BigDecimal balance = opening.get(account.getId()); for (LocalDate day = firstDay; !day.isAfter(lastDay); day = day.plusDays(1)) { Instant dayEnd = day.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(); for (FinancialTransaction transaction : active) { boolean related = transaction.getSourceAccountId() != null && transaction.getSourceAccountId().equals(account.getId()) || transaction.getDestinationAccountId() != null && transaction.getDestinationAccountId().equals(account.getId()); if (related && transaction.getOccurredAt().isBefore(dayEnd)) balance = transaction.getSourceAccountId() != null && transaction.getSourceAccountId().equals(account.getId()) ? balance.subtract(transaction.getAmount()) : balance.add(transaction.getAmount()); } values.add(new DashboardResponse.BalancePoint(day.atStartOfDay(ZoneOffset.UTC).toInstant(), balance)); } points.put(account.getId(), values); }
        List<DashboardResponse.BalanceSeries> evolution = accountList.stream().map(account -> new DashboardResponse.BalanceSeries(account.getId(), account.getName(), points.get(account.getId()))).toList();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>(); Map<String, BigDecimal> incomeByCategory = new HashMap<>(); active.stream().filter(t -> t.getType() == TransactionType.EXPENSE).forEach(t -> expenseByCategory.merge(t.getCategoryId(), t.getAmount(), BigDecimal::add)); active.stream().filter(t -> t.getType() == TransactionType.INCOME).forEach(t -> incomeByCategory.merge(t.getCategoryId(), t.getAmount(), BigDecimal::add));
        Map<String, String> categoryNames = categories.findAllByOwnerIdOrderByNameAsc(owner).stream().collect(java.util.stream.Collectors.toMap(br.com.equilibra.category.domain.Category::getId, br.com.equilibra.category.domain.Category::getName));
        List<DashboardResponse.CategoryExpense> categoryExpenses = expenseByCategory.entrySet().stream().map(e -> new DashboardResponse.CategoryExpense(categoryNames.getOrDefault(e.getKey(), "Sem categoria"), e.getValue())).toList(); List<DashboardResponse.CategoryExpense> categoryIncome = incomeByCategory.entrySet().stream().map(e -> new DashboardResponse.CategoryExpense(categoryNames.getOrDefault(e.getKey(), "Sem categoria"), e.getValue())).toList();
        return new DashboardResponse(new DashboardResponse.Period(from,to),new DashboardResponse.Summary(income,expense,income.subtract(expense),netWorth),balances,recent,evolution,categoryExpenses,categoryIncome);
    }
    private static void apply(Map<String, BigDecimal> balances, FinancialTransaction transaction) {
        if (transaction.getSourceAccountId() != null) balances.merge(transaction.getSourceAccountId(), transaction.getAmount().negate(), BigDecimal::add);
        if (transaction.getDestinationAccountId() != null) balances.merge(transaction.getDestinationAccountId(), transaction.getAmount(), BigDecimal::add);
    }
    private static BigDecimal sum(List<FinancialTransaction> list,TransactionType type){return list.stream().filter(t->t.getType()==type).map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);}
}
