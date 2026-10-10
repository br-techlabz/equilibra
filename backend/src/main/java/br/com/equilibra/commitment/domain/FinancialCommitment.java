package br.com.equilibra.commitment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="financial_commitments")
public class FinancialCommitment {
 @Id @Column(length=36,updatable=false) private String id;
 @Column(name="owner_id",nullable=false,updatable=false) private String ownerId;
 @Column(name="recurrence_rule_id") private String recurrenceRuleId;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private CommitmentType type;
 @Column(nullable=false,length=255) private String description;
 @Column(name="planned_amount",precision=19,scale=2,nullable=false) private BigDecimal plannedAmount;
 @Column(name="due_date",nullable=false) private LocalDate dueDate;
 @Column(name="account_id",nullable=false) private String accountId;
 @Column(name="category_id",nullable=false) private String categoryId;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private CommitmentStatus status=CommitmentStatus.PENDING;
 @Column(name="financial_transaction_id",unique=true) private String financialTransactionId;
 @Column(name="settled_at") private Instant settledAt;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @Version private Long version;
 protected FinancialCommitment(){}
 public FinancialCommitment(String owner,CommitmentType type,String description,BigDecimal amount,LocalDate dueDate,String accountId,String categoryId,String ruleId){id=UUID.randomUUID().toString();ownerId=owner;this.type=type;rename(description);changeAmount(amount);this.dueDate=dueDate;this.accountId=accountId;this.categoryId=categoryId;recurrenceRuleId=ruleId;if(dueDate==null)throw new IllegalArgumentException("dueDate must not be null");}
 public void rename(String value){ensurePending();if(value==null||value.isBlank()||value.trim().length()>255)throw new IllegalArgumentException("description is invalid");description=value.trim();}
 public void changeAmount(BigDecimal value){ensurePending();if(value==null||value.signum()<=0||value.scale()>2)throw new IllegalArgumentException("amount is invalid");plannedAmount=value.setScale(2);}
 public void changeDueDate(LocalDate value){ensurePending();if(value==null)throw new IllegalArgumentException("dueDate must not be null");dueDate=value;}
 public void changeReferences(String account,String category){ensurePending();accountId=account;categoryId=category;}
 public void cancel(){if(status!=CommitmentStatus.PENDING)throw new IllegalStateException("invalid transition");status=CommitmentStatus.CANCELLED;}
 public void settle(String transactionId,Instant at){if(status!=CommitmentStatus.PENDING)throw new IllegalStateException("invalid transition");if(transactionId==null||at==null)throw new IllegalArgumentException("settlement is invalid");financialTransactionId=transactionId;settledAt=at;status=CommitmentStatus.SETTLED;}
 private void ensurePending(){if(status!=CommitmentStatus.PENDING)throw new IllegalStateException("commitment is immutable");}
 @PrePersist void create(){Instant n=Instant.now();createdAt=n;updatedAt=n;} @PreUpdate void update(){updatedAt=Instant.now();}
 public String getId(){return id;} public String getOwnerId(){return ownerId;} public String getRecurrenceRuleId(){return recurrenceRuleId;} public CommitmentType getType(){return type;} public String getDescription(){return description;} public BigDecimal getPlannedAmount(){return plannedAmount;} public LocalDate getDueDate(){return dueDate;} public String getAccountId(){return accountId;} public String getCategoryId(){return categoryId;} public CommitmentStatus getStatus(){return status;} public String getFinancialTransactionId(){return financialTransactionId;} public Instant getSettledAt(){return settledAt;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
