package br.com.equilibra.attachment.application;

import br.com.equilibra.attachment.api.AttachmentResponse;
import br.com.equilibra.attachment.domain.TransactionAttachment;
import br.com.equilibra.attachment.infrastructure.TransactionAttachmentRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {
 private final TransactionAttachmentRepository attachments; private final FinancialTransactionRepository transactions; private final AttachmentStorage storage; private final AttachmentFilePolicy policy; private final AttachmentProperties properties; private final CurrentUser current;
 public AttachmentService(TransactionAttachmentRepository attachments,FinancialTransactionRepository transactions,AttachmentStorage storage,AttachmentFilePolicy policy,AttachmentProperties properties,CurrentUser current){this.attachments=attachments;this.transactions=transactions;this.storage=storage;this.policy=policy;this.properties=properties;this.current=current;}
 @Transactional public AttachmentResponse upload(String transactionId,MultipartFile file){String owner=current.id().toString();FinancialTransaction tx=transactions.findByIdAndOwnerId(transactionId,owner).orElseThrow(()->new ResourceNotFoundException("Transaction not found."));if(tx.getStatus().name().equals("CANCELLED"))throw new ResourceConflictException("Cancelled transactions cannot receive attachments.");if(attachments.countByTransactionIdAndOwnerId(transactionId,owner)>=properties.getMaxAttachmentsPerTransaction())throw new ResourceConflictException("Attachment limit reached.");try{String name=TransactionAttachment.validateFileName(file.getOriginalFilename());String type=policy.validateAndDetect(file.getInputStream(),file.getContentType(),file.getSize());String key=owner+"/"+transactionId+"/"+UUID.randomUUID();String checksum;try(InputStream in=file.getInputStream()){checksum=AttachmentFilePolicy.checksum(in);}storage.store(key,file.getInputStream(),file.getSize());try{TransactionAttachment metadata=new TransactionAttachment(owner,transactionId,name,key,type,file.getSize(),checksum);return AttachmentResponse.from(attachments.save(metadata));}catch(RuntimeException ex){try{storage.delete(key);}catch(IOException ignored){}throw ex;}}catch(IOException|IllegalArgumentException ex){throw new IllegalArgumentException("Invalid attachment file.",ex);}}
 @Transactional(readOnly=true) public List<AttachmentResponse> list(String transactionId){String owner=current.id().toString();if(transactions.findByIdAndOwnerId(transactionId,owner).isEmpty())throw new ResourceNotFoundException("Transaction not found.");return attachments.findAllByTransactionIdAndOwnerIdOrderByCreatedAtAsc(transactionId,owner).stream().map(AttachmentResponse::from).toList();}
 @Transactional(readOnly=true) public TransactionAttachment find(String id){return attachments.findByIdAndOwnerId(id,current.id().toString()).orElseThrow(()->new ResourceNotFoundException("Attachment not found."));}
 @Transactional public void delete(String id){TransactionAttachment a=find(id);try{storage.delete(a.getStorageKey());attachments.delete(a);}catch(IOException e){throw new IllegalStateException("Unable to delete attachment",e);}}
 public InputStream open(String id){TransactionAttachment a=find(id);try{return storage.open(a.getStorageKey());}catch(IOException e){throw new IllegalStateException("Unable to open attachment",e);}}
}
