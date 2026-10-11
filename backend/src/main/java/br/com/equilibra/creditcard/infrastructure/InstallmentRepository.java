package br.com.equilibra.creditcard.infrastructure;

import br.com.equilibra.creditcard.domain.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment,String> { List<Installment> findAllByOwnerIdAndPurchaseIdOrderBySequenceNumberAsc(String ownerId,String purchaseId); List<Installment> findAllByOwnerIdAndCreditCardIdAndCycleMonth(String ownerId,String card,java.time.LocalDate cycle); List<Installment> findAllByOwnerIdAndStatementIdOrderBySequenceNumberAsc(String ownerId,String statementId); }
