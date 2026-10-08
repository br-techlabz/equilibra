package br.com.equilibra.report.audit.api;

import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.domain.TagSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AuditReportResponse(List<AuditTransaction> content,int page,int size,long totalElements,int totalPages){
 public record AuditTransaction(String id,TransactionType type,TransactionStatus status,String description,Instant occurredAt,Instant createdAt,Instant updatedAt,BigDecimal amount,String categoryId,String sourceAccountId,String destinationAccountId,String notes,List<TagSummary> tags,long attachmentCount){}
}
