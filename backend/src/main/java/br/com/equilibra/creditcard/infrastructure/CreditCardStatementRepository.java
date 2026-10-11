package br.com.equilibra.creditcard.infrastructure;
import br.com.equilibra.creditcard.domain.*; import org.springframework.data.jpa.repository.*; import jakarta.persistence.LockModeType; import java.time.LocalDate; import java.util.*;
public interface CreditCardStatementRepository extends JpaRepository<CreditCardStatement,String>{
 Optional<CreditCardStatement> findByIdAndOwnerId(String id,String ownerId);
 Optional<CreditCardStatement> findByOwnerIdAndCreditCardIdAndCycleStartAndCycleEnd(String owner,String card,LocalDate start,LocalDate end);
 List<CreditCardStatement> findAllByOwnerIdAndCreditCardIdOrderByCycleStartDesc(String owner,String card);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from CreditCardStatement s where s.ownerId=:owner and s.creditCardId=:card and s.cycleStart=:start and s.cycleEnd=:end") Optional<CreditCardStatement> lockCycle(String owner,String card,LocalDate start,LocalDate end);
}
