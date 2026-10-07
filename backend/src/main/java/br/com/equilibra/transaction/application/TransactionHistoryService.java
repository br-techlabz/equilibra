package br.com.equilibra.transaction.application;

import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.api.TransactionHistoryPageResponse;
import br.com.equilibra.transaction.api.TransactionHistoryResponse;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionHistoryService {
    private final FinancialTransactionRepository repository; private final br.com.equilibra.attachment.infrastructure.TransactionAttachmentRepository attachments;
    private final AssetAccountRepository accounts;
    private final CategoryRepository categories; private final br.com.equilibra.tag.infrastructure.TagRepository tags;
    private final CurrentUser current;

    public TransactionHistoryService(FinancialTransactionRepository repository, br.com.equilibra.attachment.infrastructure.TransactionAttachmentRepository attachments, AssetAccountRepository accounts,
                                     CategoryRepository categories, br.com.equilibra.tag.infrastructure.TagRepository tags, CurrentUser current) {
        this.repository = repository;
        this.attachments = attachments;
        this.accounts = accounts;
        this.categories = categories;
        this.tags = tags;
        this.current = current;
    }

    @Transactional(readOnly = true)
    public TransactionHistoryPageResponse list(int page, int size, TransactionType type, TransactionStatus status,
                                               Instant from, Instant to, String accountId, String categoryId, java.util.List<String> tagIds) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination parameters.");
        if (from != null && to != null && !from.isBefore(to)) throw new IllegalArgumentException("from must be before to");
        validateOptionalUuid(accountId, "accountId");
        validateOptionalUuid(categoryId, "categoryId"); if (tagIds != null) tagIds.forEach(id -> validateOptionalUuid(id, "tagId"));
        String owner = current.id().toString();
        if (tagIds != null && !tagIds.isEmpty() && tags.findAllByOwnerIdAndIdIn(owner, tagIds).size() != new java.util.HashSet<>(tagIds).size()) throw new ResourceNotFoundException("Tag not found.");
        if (accountId != null && accounts.findByIdAndOwnerId(accountId, owner).isEmpty()) {
            throw new ResourceNotFoundException("Asset account not found.");
        }
        if (categoryId != null && categories.findByIdAndOwnerId(categoryId, owner).isEmpty()) {
            throw new ResourceNotFoundException("Category not found.");
        }
        Page<FinancialTransaction> result = repository.findHistory(owner, type, status, from, to, accountId, categoryId, tagIds,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        java.util.Map<String,Long> attachmentCounts = result.getContent().isEmpty()?java.util.Map.of():attachments.countByTransactionIds(owner,result.getContent().stream().map(FinancialTransaction::getId).toList()).stream().collect(java.util.stream.Collectors.toMap(row->(String)row[0],row->((Number)row[1]).longValue()));
        return new TransactionHistoryPageResponse(result.getContent().stream().map(t -> TransactionHistoryResponse.from(t, transactionTags(t), attachmentCounts.getOrDefault(t.getId(),0L))).toList(),
            result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }


    private java.util.List<br.com.equilibra.transaction.domain.TagSummary> transactionTags(FinancialTransaction t){if(t.getTagIds().isEmpty())return java.util.List.of();return tags.findAllByOwnerIdAndIdIn(t.getOwnerId(),t.getTagIds()).stream().sorted(java.util.Comparator.comparing(br.com.equilibra.tag.domain.Tag::getName)).map(v->new br.com.equilibra.transaction.domain.TagSummary(v.getId(),v.getName(),v.isActive())).toList();}

    private static void validateOptionalUuid(String value, String field) {
        if (value != null) {
            try { UUID.fromString(value); }
            catch (IllegalArgumentException exception) { throw new IllegalArgumentException(field + " must be a valid UUID.", exception); }
        }
    }
}
