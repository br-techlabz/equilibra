package br.com.equilibra.dashboard.application;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardQueryService {
    private final AssetAccountRepository accounts;
    private final FinancialTransactionRepository transactions;
    private final CurrentUser currentUser;
    public DashboardQueryService(AssetAccountRepository accounts, FinancialTransactionRepository transactions, CurrentUser currentUser){this.accounts=accounts;this.transactions=transactions;this.currentUser=currentUser;}
    @Transactional(readOnly=true)
    public DashboardResponse query(Instant from, Instant to){
        if(from==null||to==null||!from.isBefore(to))throw new IllegalArgumentException("from must be before to");
        String owner=currentUser.id().toString();
        List<AssetAccount> accountList=accounts.findAllByOwnerIdAndActiveTrueOrderByNameAsc(owner);
        List<FinancialTransaction> active=transactions.findAllByOwnerIdAndStatusAndOccurredAtBetweenOrderByOccurredAtDesc(owner,TransactionStatus.ACTIVE,from,to);
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
        return new DashboardResponse(new DashboardResponse.Period(from,to),new DashboardResponse.Summary(income,expense,income.subtract(expense),netWorth),balances,recent);
    }
    private static BigDecimal sum(List<FinancialTransaction> list,TransactionType type){return list.stream().filter(t->t.getType()==type).map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);}
}
