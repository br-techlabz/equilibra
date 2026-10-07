package br.com.equilibra.attachment.infrastructure;

import br.com.equilibra.attachment.domain.TransactionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransactionAttachmentRepository extends JpaRepository<TransactionAttachment,String>{
 Optional<TransactionAttachment> findByIdAndOwnerId(String id,String ownerId);
 List<TransactionAttachment> findAllByTransactionIdAndOwnerIdOrderByCreatedAtAsc(String transactionId,String ownerId);
 long countByTransactionIdAndOwnerId(String transactionId,String ownerId);
 @org.springframework.data.jpa.repository.Query("select a.transactionId, count(a) from TransactionAttachment a where a.ownerId = :ownerId and a.transactionId in :transactionIds group by a.transactionId")
 java.util.List<Object[]> countByTransactionIds(String ownerId, java.util.Collection<String> transactionIds);
}
