package br.com.equilibra.attachment.api;

import br.com.equilibra.attachment.domain.TransactionAttachment;
import java.time.Instant;

public record AttachmentResponse(String id,String originalFileName,String contentType,long sizeBytes,Instant createdAt){
 public static AttachmentResponse from(TransactionAttachment a){return new AttachmentResponse(a.getId(),a.getOriginalFileName(),a.getContentType(),a.getSizeBytes(),a.getCreatedAt());}
}
