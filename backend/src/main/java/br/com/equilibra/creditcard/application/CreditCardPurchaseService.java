package br.com.equilibra.creditcard.application;

import br.com.equilibra.category.domain.*;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.creditcard.api.*;
import br.com.equilibra.creditcard.domain.*;
import br.com.equilibra.creditcard.infrastructure.*;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*; import java.time.*; import java.util.*;

@Service
public class CreditCardPurchaseService {
 private final CreditCardRepository cards; private final CategoryRepository categories; private final CreditCardPurchaseRepository purchases; private final InstallmentRepository installments; private final CurrentUser currentUser;
 public CreditCardPurchaseService(CreditCardRepository c,CategoryRepository cat,CreditCardPurchaseRepository p,InstallmentRepository i,CurrentUser u){cards=c;categories=cat;purchases=p;installments=i;currentUser=u;}
 @Transactional public CreditCardPurchaseResponse create(CreateCreditCardPurchaseRequest r){String owner=owner(); CreditCard card=cards.findByIdAndOwnerIdForUpdate(r.creditCardId(),owner).orElseThrow(()->new ResourceNotFoundException("Credit card not found.")); if(card.getStatus()!=CreditCardStatus.ACTIVE)throw new ResourceConflictException("Credit card is inactive."); Category category=categories.findByIdAndOwnerId(r.categoryId(),owner).orElseThrow(()->new ResourceNotFoundException("Category not found.")); if(!category.isActive()||(category.getApplicability()!=CategoryApplicability.EXPENSE&&category.getApplicability()!=CategoryApplicability.BOTH))throw new ResourceConflictException("Category is not applicable to expenses."); BigDecimal committed=purchases.findAllByOwnerIdAndCreditCardIdOrderByPurchaseDateDesc(owner,card.getId()).stream().filter(x->x.getStatus()==CreditCardPurchaseStatus.ACTIVE).map(CreditCardPurchase::getTotalAmount).reduce(BigDecimal.ZERO,BigDecimal::add); if(committed.add(r.totalAmount()).compareTo(card.getTotalLimit())>0)throw new ResourceConflictException("Credit card limit is insufficient."); LocalDate cycle=cycle(r.purchaseDate(),card.getClosingDay()); CreditCardPurchase p=new CreditCardPurchase(owner,card.getId(),category.getId(),r.description(),r.totalAmount(),r.purchaseDate(),r.installmentCount(),cycle); purchases.save(p); long cents=r.totalAmount().movePointRight(2).longValueExact(); long base=cents/r.installmentCount(); long remainder=cents%r.installmentCount(); List<Installment> result=new ArrayList<>(); for(int n=1;n<=r.installmentCount();n++){long value=base+(n<=remainder?1:0); result.add(new Installment(owner,p.getId(),card.getId(),n,r.installmentCount(),BigDecimal.valueOf(value,2),cycle.plusMonths(n-1)));} installments.saveAll(result); return CreditCardPurchaseResponse.from(p,result);}
 @Transactional(readOnly=true) public CreditCardPurchaseResponse get(String id){CreditCardPurchase p=find(id);return CreditCardPurchaseResponse.from(p,installments.findAllByOwnerIdAndPurchaseIdOrderBySequenceNumberAsc(owner(),p.getId()));}
 @Transactional(readOnly=true) public List<CreditCardPurchaseResponse> list(String cardId){String owner=owner();cards.findByIdAndOwnerId(cardId,owner).orElseThrow(()->new ResourceNotFoundException("Credit card not found."));return purchases.findAllByOwnerIdAndCreditCardIdOrderByPurchaseDateDesc(owner,cardId).stream().map(p->CreditCardPurchaseResponse.from(p,installments.findAllByOwnerIdAndPurchaseIdOrderBySequenceNumberAsc(owner,p.getId()))).toList();}
 @Transactional public CreditCardPurchaseResponse cancel(String id){CreditCardPurchase p=find(id);p.cancel();purchases.save(p);return CreditCardPurchaseResponse.from(p,installments.findAllByOwnerIdAndPurchaseIdOrderBySequenceNumberAsc(owner(),p.getId()));}
 private CreditCardPurchase find(String id){return purchases.findByIdAndOwnerId(id,owner()).orElseThrow(()->new ResourceNotFoundException("Credit card purchase not found."));}
 private String owner(){return currentUser.id().toString();}
 static LocalDate cycle(LocalDate date,int closing){return date.getDayOfMonth()<closing?date.withDayOfMonth(1):date.plusMonths(1).withDayOfMonth(1);}
}
