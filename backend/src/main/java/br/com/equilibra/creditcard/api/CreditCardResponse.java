package br.com.equilibra.creditcard.api;

import br.com.equilibra.creditcard.domain.CreditCard;
import br.com.equilibra.creditcard.domain.CreditCardStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record CreditCardResponse(String id, String name, String institution, String nickname, BigDecimal totalLimit,
                                 BigDecimal committedLimit, BigDecimal reservedLimit, BigDecimal availableLimit,
                                 Integer closingDay, Integer dueDay, String timezone, CreditCardStatus status,
                                 Instant createdAt, Instant updatedAt) {
    public static CreditCardResponse from(CreditCard card) {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        return new CreditCardResponse(card.getId(), card.getName(), card.getInstitution(), card.getNickname(), card.getTotalLimit(), zero, zero, card.getTotalLimit(), card.getClosingDay(), card.getDueDay(), card.getTimezone(), card.getStatus(), card.getCreatedAt(), card.getUpdatedAt());
    }
}
