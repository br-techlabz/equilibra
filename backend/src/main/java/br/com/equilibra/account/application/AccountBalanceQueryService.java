package br.com.equilibra.account.application;

import br.com.equilibra.account.api.AssetAccountResponse;
import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


@Service
public class AccountBalanceQueryService {
    private final AssetAccountRepository accounts;
    private final FinancialTransactionRepository transactions;
    private final CurrentUser currentUser;
    public AccountBalanceQueryService(AssetAccountRepository accounts,FinancialTransactionRepository transactions,CurrentUser currentUser){this.accounts=accounts;this.transactions=transactions;this.currentUser=currentUser;}
    @Transactional(readOnly = true)
    public List<AssetAccountResponse> list(boolean includeInactive) {
        String owner = currentUser.id().toString();
        List<AssetAccount> values = includeInactive
            ? accounts.findAllByOwnerIdOrderByNameAsc(owner)
            : accounts.findAllByOwnerIdAndActiveTrueOrderByNameAsc(owner);

        Map<String, BigDecimal> effects = new HashMap<>();
        transactions.findAllByOwnerIdAndStatusOrderByOccurredAtDesc(owner, TransactionStatus.ACTIVE)
            .forEach(transaction -> {
                if (transaction.getSourceAccountId() != null) {
                    effects.merge(transaction.getSourceAccountId(), transaction.getAmount().negate(), BigDecimal::add);
                }
                if (transaction.getDestinationAccountId() != null) {
                    effects.merge(transaction.getDestinationAccountId(), transaction.getAmount(), BigDecimal::add);
                }
            });

        return values.stream()
            .map(account -> AssetAccountResponse.from(account,
                account.getInitialBalance().add(effects.getOrDefault(account.getId(), BigDecimal.ZERO))))
            .toList();
    }
}
