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
        java.util.List<String> normalizedTagIds = tagIds == null || tagIds.isEmpty() ? null : tagIds;
        Page<FinancialTransaction> result = repository.findHistory(owner, type, status, from, to, accountId, categoryId, normalizedTagIds,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        java.util.List<FinancialTransaction> transactions = result.getContent();
        java.util.Map<String,Long> attachmentCounts = transactions.isEmpty()?java.util.Map.of():attachments.countByTransactionIds(owner,transactions.stream().map(FinancialTransaction::getId).toList()).stream().collect(java.util.stream.Collectors.toMap(row->(String)row[0],row->((Number)row[1]).longValue()));
        java.util.Set<String> tagIdsForPage = transactions.stream().flatMap(t -> t.getTagIds().stream()).collect(java.util.stream.Collectors.toSet());
        java.util.Map<String,br.com.equilibra.tag.domain.Tag> tagsById = tagIdsForPage.isEmpty()?java.util.Map.of():tags.findAllByOwnerIdAndIdIn(owner,tagIdsForPage).stream().collect(java.util.stream.Collectors.toMap(br.com.equilibra.tag.domain.Tag::getId,java.util.function.Function.identity()));
        java.util.Map<String,java.util.List<br.com.equilibra.transaction.domain.TagSummary>> summariesByTransaction = transactions.stream().collect(java.util.stream.Collectors.toMap(FinancialTransaction::getId,t -> t.getTagIds().stream().map(tagsById::get).filter(java.util.Objects::nonNull).sorted(java.util.Comparator.comparing(br.com.equilibra.tag.domain.Tag::getName)).map(v -> new br.com.equilibra.transaction.domain.TagSummary(v.getId(),v.getName(),v.isActive())).toList()));
        return new TransactionHistoryPageResponse(transactions.stream().map(t -> TransactionHistoryResponse.from(t, summariesByTransaction.getOrDefault(t.getId(),java.util.List.of()),attachmentCounts.getOrDefault(t.getId(),0L))).toList(),
            result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }


    private static void validateOptionalUuid(String value, String field) {
        if (value != null) {
            try { UUID.fromString(value); }
            catch (IllegalArgumentException exception) { throw new IllegalArgumentException(field + " must be a valid UUID.", exception); }
        }
    }
}
