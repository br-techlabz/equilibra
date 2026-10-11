package br.com.equilibra.creditcard.application;

import br.com.equilibra.creditcard.api.*;
import br.com.equilibra.creditcard.domain.CreditCard;
import br.com.equilibra.creditcard.domain.CreditCardStatus;
import br.com.equilibra.creditcard.infrastructure.CreditCardRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CreditCardService {
    private final CreditCardRepository repository;
    private final CurrentUser currentUser;
    public CreditCardService(CreditCardRepository repository, CurrentUser currentUser) { this.repository = repository; this.currentUser = currentUser; }

    @Transactional public CreditCardResponse create(CreateCreditCardRequest request) { return CreditCardResponse.from(repository.save(new CreditCard(owner(), request.name(), request.institution(), request.nickname(), request.totalLimit(), request.closingDay(), request.dueDay(), request.timezone()))); }
    @Transactional(readOnly = true) public List<CreditCardResponse> list(boolean includeInactive) { String owner = owner(); var cards = includeInactive ? repository.findAllByOwnerIdOrderByNameAsc(owner) : repository.findAllByOwnerIdAndStatusOrderByNameAsc(owner, CreditCardStatus.ACTIVE); return cards.stream().map(CreditCardResponse::from).toList(); }
    @Transactional(readOnly = true) public CreditCardResponse get(String id) { return CreditCardResponse.from(findOwned(id)); }
    @Transactional public CreditCardResponse update(String id, UpdateCreditCardRequest request) { CreditCard card = findOwned(id); card.changeDetails(request.name(), request.institution(), request.nickname(), request.totalLimit(), request.closingDay(), request.dueDay(), request.timezone()); return CreditCardResponse.from(repository.save(card)); }
    @Transactional public CreditCardResponse deactivate(String id) { CreditCard card = findOwned(id); card.deactivate(); return CreditCardResponse.from(repository.save(card)); }
    @Transactional public CreditCardResponse activate(String id) { CreditCard card = findOwned(id); card.activate(); return CreditCardResponse.from(repository.save(card)); }

    private CreditCard findOwned(String id) { try { UUID.fromString(id); } catch (Exception e) { throw new ResourceNotFoundException("Credit card not found."); } return repository.findByIdAndOwnerId(id, owner()).orElseThrow(() -> new ResourceNotFoundException("Credit card not found.")); }
    private String owner() { return currentUser.id().toString(); }
}
