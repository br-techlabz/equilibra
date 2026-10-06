package br.com.equilibra.transaction.infrastructure;

import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, String> {

    Optional<FinancialTransaction> findByIdAndOwnerId(String id, String ownerId);

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
