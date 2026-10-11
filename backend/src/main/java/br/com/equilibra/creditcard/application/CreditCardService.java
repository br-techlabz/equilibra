package br.com.equilibra.creditcard.application;

import br.com.equilibra.creditcard.api.*;
import br.com.equilibra.creditcard.domain.CreditCard;
import br.com.equilibra.creditcard.domain.CreditCardStatus;
import br.com.equilibra.creditcard.infrastructure.CreditCardRepository;
import br.com.equilibra.creditcard.infrastructure.CreditCardPurchaseRepository;
import br.com.equilibra.creditcard.domain.CreditCardPurchaseStatus;
import br.com.equilibra.creditcard.domain.StatementPaymentStatus;
import br.com.equilibra.creditcard.infrastructure.CreditCardStatementRepository;
import br.com.equilibra.creditcard.infrastructure.StatementPaymentRepository;
import java.math.BigDecimal;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {
    private final CreditCardRepository repository;
    private final CreditCardPurchaseRepository purchases;
    private final CreditCardStatementRepository statements;
    private final StatementPaymentRepository payments;
    private final CurrentUser currentUser;
    public CreditCardService(CreditCardRepository repository, CreditCardPurchaseRepository purchases, CreditCardStatementRepository statements, StatementPaymentRepository payments, CurrentUser currentUser) { this.repository = repository; this.purchases = purchases; this.statements = statements; this.payments = payments; this.currentUser = currentUser; }

    @Transactional public CreditCardResponse create(CreateCreditCardRequest request) { return response(repository.save(new CreditCard(owner(), request.name(), request.institution(), request.nickname(), request.totalLimit(), request.closingDay(), request.dueDay(), request.timezone()))); }
    @Transactional(readOnly = true) public List<CreditCardResponse> list(boolean includeInactive) { String owner = owner(); var cards = includeInactive ? repository.findAllByOwnerIdOrderByNameAsc(owner) : repository.findAllByOwnerIdAndStatusOrderByNameAsc(owner, CreditCardStatus.ACTIVE); return cards.stream().map(this::response).toList(); }
    @Transactional(readOnly = true) public CreditCardResponse get(String id) { return response(findOwned(id)); }
    @Transactional public CreditCardResponse update(String id, UpdateCreditCardRequest request) { CreditCard card = findOwned(id); card.changeDetails(request.name(), request.institution(), request.nickname(), request.totalLimit(), request.closingDay(), request.dueDay(), request.timezone()); return CreditCardResponse.from(repository.save(card)); }
    @Transactional public CreditCardResponse deactivate(String id) { CreditCard card = findOwned(id); card.deactivate(); return CreditCardResponse.from(repository.save(card)); }
    @Transactional public CreditCardResponse activate(String id) { CreditCard card = findOwned(id); card.activate(); return CreditCardResponse.from(repository.save(card)); }

    private CreditCard findOwned(String id) { try { UUID.fromString(id); } catch (Exception e) { throw new ResourceNotFoundException("Credit card not found."); } return repository.findByIdAndOwnerId(id, owner()).orElseThrow(() -> new ResourceNotFoundException("Credit card not found.")); }
    private CreditCardResponse response(CreditCard card) { BigDecimal committed = purchases.findAllByOwnerIdAndCreditCardIdOrderByPurchaseDateDesc(owner(), card.getId()).stream().filter(p -> p.getStatus() == CreditCardPurchaseStatus.ACTIVE).map(p -> p.getTotalAmount()).reduce(BigDecimal.ZERO, BigDecimal::add); var cardStatements = statements.findAllByOwnerIdAndCreditCardId(owner(), card.getId()); var statementIds = cardStatements.stream().map(s -> s.getId()).toList(); BigDecimal paid = statementIds.isEmpty() ? BigDecimal.ZERO : payments.findAllByOwnerIdAndStatementIdInAndStatus(owner(), statementIds, StatementPaymentStatus.ACTIVE).stream().map(p -> p.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add); committed = committed.subtract(paid).max(BigDecimal.ZERO).setScale(2); BigDecimal available = card.getTotalLimit().subtract(committed).max(BigDecimal.ZERO).setScale(2); return new CreditCardResponse(card.getId(), card.getName(), card.getInstitution(), card.getNickname(), card.getTotalLimit(), committed, BigDecimal.ZERO.setScale(2), available, card.getClosingDay(), card.getDueDay(), card.getTimezone(), card.getStatus(), card.getCreatedAt(), card.getUpdatedAt()); }
    private String owner() { return currentUser.id().toString(); }
}
