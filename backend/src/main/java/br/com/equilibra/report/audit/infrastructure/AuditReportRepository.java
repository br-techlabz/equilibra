package br.com.equilibra.report.audit.infrastructure;

import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;

public interface AuditReportRepository extends JpaRepository<FinancialTransaction,String>{
 @Query("select distinct t from FinancialTransaction t left join fetch t.tagIds where t.ownerId=:owner and (:type is null or t.type=:type) and (:status is null or t.status=:status) and t.occurredAt>=:from and t.occurredAt<:to and (:accountId is null or t.sourceAccountId=:accountId or t.destinationAccountId=:accountId) and (:categoryId is null or t.categoryId=:categoryId) and (:tagIds is null or exists (select 1 from FinancialTransaction t2 join t2.tagIds tagId where t2.id=t.id and tagId in :tagIds))")
 Page<FinancialTransaction> findPage(@Param("owner")String owner,@Param("type")TransactionType type,@Param("status")TransactionStatus status,@Param("from")Instant from,@Param("to")Instant to,@Param("accountId")String accountId,@Param("categoryId")String categoryId,@Param("tagIds")Collection<String> tagIds,Pageable pageable);
}
