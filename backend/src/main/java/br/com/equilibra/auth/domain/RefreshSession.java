package br.com.equilibra.auth.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="refresh_sessions")
public class RefreshSession {
 @Id @Column(length=36,nullable=false,updatable=false) private String id;
 @Column(name="owner_id",length=36,nullable=false,updatable=false) private String ownerId;
 @Column(name="token_hash",length=64,nullable=false,unique=true,updatable=false) private String tokenHash;
 @Column(name="expires_at",nullable=false) private Instant expiresAt;
 @Column(name="revoked_at") private Instant revokedAt;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="rotated_at") private Instant rotatedAt;
 protected RefreshSession(){}
 public RefreshSession(String ownerId,String tokenHash,Instant expiresAt,Instant now){this.id=UUID.randomUUID().toString();this.ownerId=ownerId;this.tokenHash=tokenHash;this.expiresAt=expiresAt;this.createdAt=now;}
 public boolean usableAt(Instant now){return revokedAt==null&&expiresAt.isAfter(now);}
 public void rotate(Instant now){this.rotatedAt=now;this.revokedAt=now;}
 public String getId(){return id;} public String getOwnerId(){return ownerId;} public String getTokenHash(){return tokenHash;} public Instant getExpiresAt(){return expiresAt;} public Instant getRevokedAt(){return revokedAt;} public Instant getCreatedAt(){return createdAt;} public Instant getRotatedAt(){return rotatedAt;}
}
