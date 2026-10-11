package br.com.equilibra.creditcard.application;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.creditcard.api.StatementPaymentRequest;
import br.com.equilibra.creditcard.domain.*;
import br.com.equilibra.creditcard.infrastructure.*;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.*;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatementPaymentService {
    private final CreditCardStatementRepository statements;
    private final CreditCardStatementService statementService;
    private final AssetAccountRepository accounts;
    private final FinancialTransactionRepository transactions;
    private final StatementPaymentRepository payments;
    private final CurrentUser current;

    public StatementPaymentService(CreditCardStatementRepository statements, CreditCardStatementService statementService,
                                   AssetAccountRepository accounts, FinancialTransactionRepository transactions,
                                   StatementPaymentRepository payments, CurrentUser current) {
        this.statements = statements;
        this.statementService = statementService;
        this.accounts = accounts;
        this.transactions = transactions;
        this.payments = payments;
        this.current = current;
    }

    @Transactional
    public StatementPayment pay(String statementId, StatementPaymentRequest request, String key) {
        String owner = current.id().toString();
        if (key != null) {
            var existing = payments.findByOwnerIdAndIdempotencyKey(owner, key);
            if (existing.isPresent()) return existing.get();
        }
        CreditCardStatement statement = statements.findByIdAndOwnerId(statementId, owner)
            .orElseThrow(() -> new ResourceNotFoundException("Statement not found."));
        return payPersisted(statement, request, key, owner);
    }

    @Transactional
    public StatementPayment payCurrent(String cardId, StatementPaymentRequest request, String key) {
        String owner = current.id().toString();
        if (key != null) {
            var existing = payments.findByOwnerIdAndIdempotencyKey(owner, key);
            if (existing.isPresent()) return existing.get();
        }
        CreditCardStatement statement = statementService.ensureCurrentPersisted(cardId);
        return payPersisted(statement, request, key, owner);
    }

    private StatementPayment payPersisted(CreditCardStatement statement, StatementPaymentRequest request,
                                          String key, String owner) {
        if (statement.getStatus() == CreditCardStatementStatus.PAID
            || statement.getStatus() == CreditCardStatementStatus.CANCELLED) {
            throw new ResourceConflictException("Statement is not payable.");
        }
        AssetAccount account = accounts.findByIdAndOwnerId(request.bankAccountId(), owner)
            .orElseThrow(() -> new ResourceNotFoundException("Asset account not found."));
        if (!account.isActive()) throw new ResourceConflictException("Asset account is inactive.");
        statement.registerPayment(request.amount());
        FinancialTransaction transaction = transactions.save(FinancialTransaction.statementPayment(
            owner, "Pagamento de fatura", request.amount(), request.paidAt(), account.getId(),
            "statement=" + statement.getId()));
        StatementPayment payment = payments.save(new StatementPayment(owner, statement.getId(), account.getId(),
            request.amount(), request.paidAt(), transaction.getId(), key));
        statements.save(statement);
        return payment;
    }
}
