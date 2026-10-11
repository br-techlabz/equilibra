package br.com.equilibra.transaction.api;

import br.com.equilibra.creditcard.domain.CreditCardPurchase;
import java.time.Instant;

public final class CreditCardExpenseResponse {
    private CreditCardExpenseResponse() {}
    public static ExpenseResponse from(CreditCardPurchase purchase) {
        return new ExpenseResponse(purchase.getId(), purchase.getDescription(), purchase.getPurchaseAt() != null ? purchase.getPurchaseAt() : purchase.getPurchaseDate().atStartOfDay(java.time.ZoneId.of("America/Sao_Paulo")).toInstant(), null, purchase.getTotalAmount(), "CREDIT_CARD", purchase.getCreditCardId(), purchase.getInstallmentCount(), purchase.getCycleMonth(), purchase.getPurchaseAt(), purchase.getCategoryId(), null, purchase.getStatus() == br.com.equilibra.creditcard.domain.CreditCardPurchaseStatus.ACTIVE ? br.com.equilibra.transaction.domain.TransactionStatus.ACTIVE : br.com.equilibra.transaction.domain.TransactionStatus.CANCELLED, purchase.getCreatedAt(), purchase.getUpdatedAt(), java.util.List.of());
    }
}
