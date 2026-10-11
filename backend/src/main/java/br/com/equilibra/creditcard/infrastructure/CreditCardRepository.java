package br.com.equilibra.creditcard.infrastructure;

import br.com.equilibra.creditcard.domain.CreditCard;
import br.com.equilibra.creditcard.domain.CreditCardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, String> {
    Optional<CreditCard> findByIdAndOwnerId(String id, String ownerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CreditCard c where c.id = :id and c.ownerId = :ownerId")
    Optional<CreditCard> findByIdAndOwnerIdForUpdate(String id, String ownerId);
    List<CreditCard> findAllByOwnerIdOrderByNameAsc(String ownerId);
    List<CreditCard> findAllByOwnerIdAndStatusOrderByNameAsc(String ownerId, CreditCardStatus status);
}
