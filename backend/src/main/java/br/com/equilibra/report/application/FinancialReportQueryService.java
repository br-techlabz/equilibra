package br.com.equilibra.report.application;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.report.api.FinancialReportResponse;
import br.com.equilibra.report.api.FinancialReportTransaction;
import br.com.equilibra.report.infrastructure.FinancialReportRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class FinancialReportQueryService {
    private final AssetAccountRepository accounts;
    private final FinancialReportRepository transactions;
    private final CurrentUser currentUser;

    public FinancialReportQueryService(AssetAccountRepository accounts, FinancialReportRepository transactions, CurrentUser currentUser) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public FinancialReportResponse query(Instant from, Instant to, List<String> accountIds, int page, int size) {
        if (from == null || to == null || !from.isBefore(to)) throw new IllegalArgumentException("from must be before to");
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination parameters.");
        String owner = currentUser.id().toString();
        List<String> selectedIds = normalizeIds(accountIds);
        List<AssetAccount> selected = selectedIds.isEmpty() ? accounts.findAllByOwnerIdOrderByNameAsc(owner) : selectedIds.stream().map(id -> accounts.findByIdAndOwnerId(id, owner).orElseThrow(() -> new ResourceNotFoundException("Asset account not found."))).toList();
        Set<String> selectedSet = selected.stream().map(AssetAccount::getId).collect(java.util.stream.Collectors.toSet());
        List<String> ids = selected.stream().map(AssetAccount::getId).toList();
        BigDecimal opening = selected.stream().map(AssetAccount::getInitialBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        opening = opening.add(effects(transactions.findActiveBefore(owner, TransactionStatus.ACTIVE, from, ids), selectedSet));
        List<FinancialTransaction> within = transactions.findActiveWithin(owner, TransactionStatus.ACTIVE, from, to, ids);
        BigDecimal income = sumType(within, TransactionType.INCOME, selectedSet, true);
        BigDecimal expense = sumType(within, TransactionType.EXPENSE, selectedSet, false);
        BigDecimal incoming = transferBoundary(within, selectedSet, true);
        BigDecimal outgoing = transferBoundary(within, selectedSet, false);
        BigDecimal internal = within.stream().filter(t -> t.getType() == TransactionType.TRANSFER && selectedSet.contains(t.getSourceAccountId()) && selectedSet.contains(t.getDestinationAccountId())).map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal result = income.subtract(expense);
        BigDecimal change = result.add(incoming).subtract(outgoing);
        Page<FinancialTransaction> details = transactions.findDetails(owner, from, to, ids, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        List<FinancialReportTransaction> content = details.getContent().stream().map(this::detail).toList();
        return new FinancialReportResponse(new FinancialReportResponse.Period(from, to), selected.stream().map(a -> new FinancialReportResponse.AccountSelection(a.getId(), a.getName(), a.isActive())).toList(), opening, income, expense, result, incoming, outgoing, internal, change, opening.add(change), new FinancialReportResponse.Details(content, details.getNumber(), details.getSize(), details.getTotalElements(), details.getTotalPages()));
    }

    private BigDecimal effects(List<FinancialTransaction> values, Set<String> selected) {
        return values.stream().map(t -> { BigDecimal effect = BigDecimal.ZERO; if (selected.contains(t.getDestinationAccountId())) effect = effect.add(t.getAmount()); if (selected.contains(t.getSourceAccountId())) effect = effect.subtract(t.getAmount()); return effect; }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal sumType(List<FinancialTransaction> values, TransactionType type, Set<String> selected, boolean destination) { return values.stream().filter(t -> t.getType() == type && selected.contains(destination ? t.getDestinationAccountId() : t.getSourceAccountId())).map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private BigDecimal transferBoundary(List<FinancialTransaction> values, Set<String> selected, boolean incoming) { return values.stream().filter(t -> t.getType() == TransactionType.TRANSFER).filter(t -> incoming ? !selected.contains(t.getSourceAccountId()) && selected.contains(t.getDestinationAccountId()) : selected.contains(t.getSourceAccountId()) && !selected.contains(t.getDestinationAccountId())).map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private FinancialReportTransaction detail(FinancialTransaction t) { return new FinancialReportTransaction(t.getId(), t.getType(), t.getStatus(), t.getDescription(), t.getOccurredAt(), t.getAmount(), t.getCategoryId(), t.getSourceAccountId(), t.getDestinationAccountId(), t.getNotes()); }
    private static List<String> normalizeIds(List<String> ids) { return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).distinct().toList(); }
}
