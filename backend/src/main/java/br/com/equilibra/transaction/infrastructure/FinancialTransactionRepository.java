package br.com.equilibra.transaction.infrastructure;

import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, String> {

    Optional<FinancialTransaction> findByIdAndOwnerId(String id, String ownerId);

    Optional<FinancialTransaction> findByIdAndOwnerIdAndType(String id, String ownerId, TransactionType type);

    @org.springframework.data.jpa.repository.Query("select t from FinancialTransaction t where t.ownerId = :ownerId and t.type = :type and (:status is null or t.status = :status)")
    Page<FinancialTransaction> findPageByOwnerAndTypeAndStatus(String ownerId, TransactionType type, TransactionStatus status, Pageable pageable);

    List<FinancialTransaction> findAllByOwnerIdOrderByOccurredAtDesc(String ownerId);

    List<FinancialTransaction> findAllByOwnerIdAndStatusOrderByOccurredAtDesc(String ownerId, TransactionStatus status);

    List<FinancialTransaction> findAllByOwnerIdAndOccurredAtBetweenOrderByOccurredAtDesc(
        String ownerId, Instant from, Instant to
    );

    List<FinancialTransaction> findAllByOwnerIdAndTypeAndOccurredAtBetweenOrderByOccurredAtDesc(
        String ownerId, TransactionType type, Instant from, Instant to
    );

    List<FinancialTransaction> findAllByOwnerIdAndStatusAndOccurredAtBetweenOrderByOccurredAtDesc(
        String ownerId, TransactionStatus status, Instant from, Instant to
    );

    List<FinancialTransaction> findAllByOwnerIdAndSourceAccountIdOrOwnerIdAndDestinationAccountIdOrderByOccurredAtDesc(
        String sourceOwnerId, String sourceAccountId, String destinationOwnerId, String destinationAccountId
    );
}
