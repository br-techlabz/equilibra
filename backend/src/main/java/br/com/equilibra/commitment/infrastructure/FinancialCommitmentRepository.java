package br.com.equilibra.commitment.infrastructure;
import br.com.equilibra.commitment.domain.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import jakarta.persistence.LockModeType; import java.time.LocalDate; import java.util.*;
public interface FinancialCommitmentRepository extends JpaRepository<FinancialCommitment,String>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) Optional<FinancialCommitment> findByIdAndOwnerId(String id,String ownerId);
 @Query("select c from FinancialCommitment c where c.ownerId=:owner and (:type is null or c.type=:type) and (:status is null or c.status=:status) and (:from is null or c.dueDate>=:from) and (:to is null or c.dueDate<=:to) and (:account is null or c.accountId=:account) and (:category is null or c.categoryId=:category) and (:rule is null or c.recurrenceRuleId=:rule)") Page<FinancialCommitment> search(String owner,CommitmentType type,CommitmentStatus status,LocalDate from,LocalDate to,String account,String category,String rule,Pageable pageable);
 @Query("select c from FinancialCommitment c where c.ownerId=:owner and c.status=br.com.equilibra.commitment.domain.CommitmentStatus.PENDING and c.dueDate>=:from and c.dueDate<=:to") List<FinancialCommitment> pendingBetween(String owner, LocalDate from, LocalDate to);
 @Query("select c from FinancialCommitment c where c.ownerId=:owner and c.status=br.com.equilibra.commitment.domain.CommitmentStatus.PENDING and c.dueDate>=:from and c.dueDate<=:to and c.accountId in :accounts") List<FinancialCommitment> pendingBetweenAccounts(String owner, LocalDate from, LocalDate to, List<String> accounts);
 Optional<FinancialCommitment> findByOwnerIdAndRecurrenceRuleIdAndDueDate(String owner,String rule,LocalDate date);
}
