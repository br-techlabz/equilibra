package br.com.equilibra.creditcard.infrastructure;
import br.com.equilibra.creditcard.domain.StatementPayment; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface StatementPaymentRepository extends JpaRepository<StatementPayment,String>{Optional<StatementPayment> findByOwnerIdAndIdempotencyKey(String owner,String key); List<StatementPayment> findAllByOwnerIdAndStatementIdOrderByPaidAtDesc(String owner,String statement);
 List<StatementPayment> findAllByOwnerIdAndStatementIdInAndStatus(String owner, Collection<String> statements, br.com.equilibra.creditcard.domain.StatementPaymentStatus status);}
