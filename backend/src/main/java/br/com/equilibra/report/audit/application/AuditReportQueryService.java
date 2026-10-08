package br.com.equilibra.report.audit.application;

import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.attachment.infrastructure.TransactionAttachmentRepository;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.report.audit.api.AuditReportResponse;
import br.com.equilibra.report.audit.infrastructure.AuditReportRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.tag.infrastructure.TagRepository;
import br.com.equilibra.transaction.domain.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditReportQueryService {
 private final AuditReportRepository repository; private final AssetAccountRepository accounts; private final CategoryRepository categories; private final TagRepository tags; private final TransactionAttachmentRepository attachments; private final CurrentUser current;
 public AuditReportQueryService(AuditReportRepository repository,AssetAccountRepository accounts,CategoryRepository categories,TagRepository tags,TransactionAttachmentRepository attachments,CurrentUser current){this.repository=repository;this.accounts=accounts;this.categories=categories;this.tags=tags;this.attachments=attachments;this.current=current;}
 @Transactional(readOnly=true)
 public AuditReportResponse list(Instant from,Instant to,List<String> accountIds,TransactionType type,TransactionStatus status,String categoryId,List<String> tagIds,int page,int size){
  if(from==null||to==null||!from.isBefore(to))throw new IllegalArgumentException("from must be before to");if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid pagination parameters.");String owner=current.id().toString();String account=validateAccounts(owner,accountIds);validateCategory(owner,categoryId);List<String> normalized=tagIds==null||tagIds.isEmpty()?null:tagIds.stream().distinct().toList();if(normalized!=null&&tags.findAllByOwnerIdAndIdIn(owner,normalized).size()!=normalized.size())throw new ResourceNotFoundException("Tag not found.");var result=repository.findPage(owner,type,status,from,to,account,categoryId,normalized,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"occurredAt").and(Sort.by(Sort.Direction.DESC,"id"))));var values=result.getContent();var counts=values.isEmpty()?Map.<String,Long>of():attachments.countByTransactionIds(owner,values.stream().map(FinancialTransaction::getId).toList()).stream().collect(Collectors.toMap(row->(String)row[0],row->((Number)row[1]).longValue()));var tagIdsPage=values.stream().flatMap(t->t.getTagIds().stream()).collect(Collectors.toSet());var tagMap=tagIdsPage.isEmpty()?Map.<String,br.com.equilibra.tag.domain.Tag>of():tags.findAllByOwnerIdAndIdIn(owner,tagIdsPage).stream().collect(Collectors.toMap(br.com.equilibra.tag.domain.Tag::getId,java.util.function.Function.identity()));return new AuditReportResponse(values.stream().map(t->new AuditReportResponse.AuditTransaction(t.getId(),t.getType(),t.getStatus(),t.getDescription(),t.getOccurredAt(),t.getCreatedAt(),t.getUpdatedAt(),t.getAmount(),t.getCategoryId(),t.getSourceAccountId(),t.getDestinationAccountId(),t.getNotes(),t.getTagIds().stream().map(tagMap::get).filter(Objects::nonNull).sorted(Comparator.comparing(br.com.equilibra.tag.domain.Tag::getName)).map(v->new TagSummary(v.getId(),v.getName(),v.isActive())).toList(),counts.getOrDefault(t.getId(),0L))).toList(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages());
 }
 private String validateAccounts(String owner,List<String> ids){if(ids==null||ids.isEmpty())return null;List<String> unique=ids.stream().distinct().toList();for(String id:unique)if(accounts.findByIdAndOwnerId(id,owner).isEmpty())throw new ResourceNotFoundException("Asset account not found.");return unique.get(0);}
 private void validateCategory(String owner,String id){if(id!=null&&categories.findByIdAndOwnerId(id,owner).isEmpty())throw new ResourceNotFoundException("Category not found.");}
}
