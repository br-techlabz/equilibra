package br.com.equilibra.report.infrastructure;

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

public interface FinancialReportRepository extends JpaRepository<FinancialTransaction, String> {
    @Query("select t from FinancialTransaction t where t.ownerId = :ownerId and t.status = :status and t.occurredAt < :before and (t.sourceAccountId in :accountIds or t.destinationAccountId in :accountIds)")
    java.util.List<FinancialTransaction> findActiveBefore(@Param("ownerId") String ownerId, @Param("status") TransactionStatus status, @Param("before") Instant before, @Param("accountIds") Collection<String> accountIds);

    @Query("select t from FinancialTransaction t where t.ownerId = :ownerId and t.status = :status and t.occurredAt >= :from and t.occurredAt < :to and (t.sourceAccountId in :accountIds or t.destinationAccountId in :accountIds)")
    java.util.List<FinancialTransaction> findActiveWithin(@Param("ownerId") String ownerId, @Param("status") TransactionStatus status, @Param("from") Instant from, @Param("to") Instant to, @Param("accountIds") Collection<String> accountIds);

    @Query("select t from FinancialTransaction t where t.ownerId = :ownerId and t.occurredAt >= :from and t.occurredAt < :to and (t.sourceAccountId in :accountIds or t.destinationAccountId in :accountIds)")
    Page<FinancialTransaction> findDetails(@Param("ownerId") String ownerId, @Param("from") Instant from, @Param("to") Instant to, @Param("accountIds") Collection<String> accountIds, Pageable pageable);
}
