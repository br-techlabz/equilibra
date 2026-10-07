package br.com.equilibra.attachment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name="transaction_attachments")
public class TransactionAttachment {
    public static final int FILE_NAME_MAX_LENGTH=255;
    @Id @Column(length=36,nullable=false,updatable=false) private String id;
    @Column(name="owner_id",length=36,nullable=false,updatable=false) private String ownerId;
    @Column(name="transaction_id",length=36,nullable=false,updatable=false) private String transactionId;
    @Column(name="original_file_name",length=FILE_NAME_MAX_LENGTH,nullable=false,updatable=false) private String originalFileName;
    @Column(name="storage_key",length=512,nullable=false,unique=true,updatable=false) private String storageKey;
    @Column(name="content_type",length=100,nullable=false,updatable=false) private String contentType;
    @Column(name="size_bytes",nullable=false,updatable=false) private long sizeBytes;
    @Column(name="checksum_sha256",length=64,nullable=false,updatable=false) private String checksumSha256;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Version private Long version;
    protected TransactionAttachment() {}
    public TransactionAttachment(String ownerId,String transactionId,String originalFileName,String storageKey,String contentType,long sizeBytes,String checksumSha256){
        this.id=UUID.randomUUID().toString(); this.ownerId=requireUuid(ownerId,"ownerId"); this.transactionId=requireUuid(transactionId,"transactionId");
        this.originalFileName=validateFileName(originalFileName); this.storageKey=requireText(storageKey,"storageKey"); this.contentType=requireText(contentType,"contentType");
        if(sizeBytes<=0) throw new IllegalArgumentException("sizeBytes must be positive"); this.sizeBytes=sizeBytes; this.checksumSha256=validateChecksum(checksumSha256);
    }
    @PrePersist void onCreate(){createdAt=Instant.now();}
    private static String requireUuid(String v,String f){try{return UUID.fromString(v).toString();}catch(Exception e){throw new IllegalArgumentException(f+" must be a valid UUID.",e);}}
    private static String requireText(String v,String f){if(v==null||v.isBlank())throw new IllegalArgumentException(f+" must not be blank");return v;}
    public static String validateFileName(String v){if(v==null||v.isBlank()||v.length()>FILE_NAME_MAX_LENGTH||v.indexOf('\r')>=0||v.indexOf('\n')>=0||v.indexOf('\0')>=0||v.contains("../")||v.contains("..\\"))throw new IllegalArgumentException("invalid original file name");return v;}
    private static String validateChecksum(String v){if(v==null||!v.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("checksumSha256 must be lowercase SHA-256 hex");return v;}
    public String getId(){return id;} public String getOwnerId(){return ownerId;} public String getTransactionId(){return transactionId;} public String getOriginalFileName(){return originalFileName;} public String getStorageKey(){return storageKey;} public String getContentType(){return contentType;} public long getSizeBytes(){return sizeBytes;} public String getChecksumSha256(){return checksumSha256;} public Instant getCreatedAt(){return createdAt;} public Long getVersion(){return version;}
    @Override public boolean equals(Object o){return this==o||o instanceof TransactionAttachment a&&Objects.equals(id,a.id);} @Override public int hashCode(){return Objects.hash(id);}
}
