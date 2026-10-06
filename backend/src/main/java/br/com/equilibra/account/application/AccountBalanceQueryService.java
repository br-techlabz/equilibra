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
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountBalanceQueryService {
    private final AssetAccountRepository accounts;
    private final FinancialTransactionRepository transactions;
    private final CurrentUser currentUser;
    public AccountBalanceQueryService(AssetAccountRepository accounts,FinancialTransactionRepository transactions,CurrentUser currentUser){this.accounts=accounts;this.transactions=transactions;this.currentUser=currentUser;}
    @Transactional(readOnly=true)
    public List<AssetAccountResponse> list(boolean includeInactive){String owner=currentUser.id().toString();List<AssetAccount> values=includeInactive?accounts.findAllByOwnerIdOrderByNameAsc(owner):accounts.findAllByOwnerIdAndActiveTrueOrderByNameAsc(owner);Map<String,BigDecimal> effects=transactions.findAllByOwnerIdAndStatusOrderByOccurredAtDesc(owner,TransactionStatus.ACTIVE).stream().collect(Collectors.groupingBy(t->t.getSourceAccountId()!=null?t.getSourceAccountId():t.getDestinationAccountId(),Collectors.reducing(BigDecimal.ZERO,t->t.getDestinationAccountId()!=null?t.getAmount():t.getAmount().negate(),BigDecimal::add)));return values.stream().map(a->AssetAccountResponse.from(a,a.getInitialBalance().add(effects.getOrDefault(a.getId(),BigDecimal.ZERO)))).toList();}
}
