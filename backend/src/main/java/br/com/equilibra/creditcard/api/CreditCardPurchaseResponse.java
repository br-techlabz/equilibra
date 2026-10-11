package br.com.equilibra.creditcard.api;

import br.com.equilibra.creditcard.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreditCardPurchaseResponse(String id,String creditCardId,String categoryId,String description,BigDecimal totalAmount,LocalDate purchaseDate,Integer installmentCount,LocalDate cycleMonth,CreditCardPurchaseStatus status,List<InstallmentResponse> installments) {
 public record InstallmentResponse(Integer sequenceNumber,Integer totalInstallments,BigDecimal principalAmount,LocalDate cycleMonth) {}
 public static CreditCardPurchaseResponse from(CreditCardPurchase p,List<br.com.equilibra.creditcard.domain.Installment> i){return new CreditCardPurchaseResponse(p.getId(),p.getCreditCardId(),p.getCategoryId(),p.getDescription(),p.getTotalAmount(),p.getPurchaseDate(),p.getInstallmentCount(),p.getCycleMonth(),p.getStatus(),i.stream().map(x->new InstallmentResponse(x.getSequenceNumber(),x.getTotalInstallments(),x.getPrincipalAmount(),x.getCycleMonth())).toList());}
}
