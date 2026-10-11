package br.com.equilibra.creditcard.infrastructure;

import br.com.equilibra.creditcard.domain.CreditCardPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CreditCardPurchaseRepository extends JpaRepository<CreditCardPurchase,String> {
 Optional<CreditCardPurchase> findByIdAndOwnerId(String id,String ownerId);
 List<CreditCardPurchase> findAllByOwnerIdAndCreditCardIdOrderByPurchaseDateDesc(String ownerId,String creditCardId);
 List<CreditCardPurchase> findAllByOwnerIdOrderByPurchaseDateDesc(String ownerId);
 List<CreditCardPurchase> findAllByOwnerIdAndStatusOrderByPurchaseDateDesc(String ownerId, br.com.equilibra.creditcard.domain.CreditCardPurchaseStatus status);
 boolean existsByOwnerIdAndCreditCardIdAndStatus(String ownerId,String creditCardId, br.com.equilibra.creditcard.domain.CreditCardPurchaseStatus status);
}
