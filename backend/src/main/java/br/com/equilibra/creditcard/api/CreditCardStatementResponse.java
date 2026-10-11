package br.com.equilibra.creditcard.api;
import br.com.equilibra.creditcard.domain.*; import java.math.BigDecimal; import java.time.*; import java.util.*;
public record CreditCardStatementResponse(String id,String creditCardId,LocalDate cycleStart,LocalDate cycleEnd,Instant closingAt,LocalDate dueDate,CreditCardStatementStatus status,BigDecimal totalAmount,BigDecimal paidAmount,BigDecimal outstandingAmount,List<Item> items){public record Item(String id,String purchaseId,String description,Integer sequenceNumber,Integer totalInstallments,BigDecimal amount,LocalDate cycleMonth){} }
