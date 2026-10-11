package br.com.equilibra.creditcard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="credit_card_statements", uniqueConstraints=@UniqueConstraint(name="uq_statement_cycle", columnNames={"owner_id","credit_card_id","cycle_start","cycle_end"}))
public class CreditCardStatement {
 @Id @Column(length=36,nullable=false,updatable=false) private String id;
 @Column(name="owner_id",length=36,nullable=false,updatable=false) private String ownerId;
 @Column(name="credit_card_id",length=36,nullable=false,updatable=false) private String creditCardId;
 @Column(name="cycle_start",nullable=false) private LocalDate cycleStart;
 @Column(name="cycle_end",nullable=false) private LocalDate cycleEnd;
 @Column(name="closing_at",nullable=false) private Instant closingAt;
 @Column(name="due_date",nullable=false) private LocalDate dueDate;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private CreditCardStatementStatus status=CreditCardStatementStatus.OPEN;
 @Column(name="total_amount",precision=19,scale=2,nullable=false) private BigDecimal totalAmount=BigDecimal.ZERO.setScale(2);
 @Column(name="paid_amount",precision=19,scale=2,nullable=false) private BigDecimal paidAmount=BigDecimal.ZERO.setScale(2);
 @Column(name="outstanding_amount",precision=19,scale=2,nullable=false) private BigDecimal outstandingAmount=BigDecimal.ZERO.setScale(2);
 @Column(name="closed_at") private Instant closedAt;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @Version private Long version;
 protected CreditCardStatement(){}
 public CreditCardStatement(String owner,String card,LocalDate start,LocalDate end,Instant closing,LocalDate due){id=UUID.randomUUID().toString();ownerId=owner;creditCardId=card;cycleStart=start;cycleEnd=end;closingAt=closing;dueDate=due;}
 @PrePersist void create(){createdAt=Instant.now();updatedAt=createdAt;} @PreUpdate void update(){updatedAt=Instant.now();}
 public void close(BigDecimal total){if(status!=CreditCardStatementStatus.OPEN)throw new IllegalStateException("statement is not open");totalAmount=money(total);paidAmount=BigDecimal.ZERO.setScale(2);outstandingAmount=totalAmount;closedAt=Instant.now();status=CreditCardStatementStatus.CLOSED;}
 public void registerPayment(BigDecimal amount){if(amount==null||amount.signum()<=0||amount.compareTo(outstandingAmount)>0)throw new IllegalArgumentException("payment exceeds outstanding amount");paidAmount=paidAmount.add(amount).setScale(2);outstandingAmount=totalAmount.subtract(paidAmount).setScale(2);}
 public boolean overdue(LocalDate today){return (status==CreditCardStatementStatus.CLOSED||status==CreditCardStatementStatus.OPEN)&&dueDate.isBefore(today)&&outstandingAmount.signum()>0;}
 private static BigDecimal money(BigDecimal v){if(v==null||v.signum()<0||v.scale()>2)throw new IllegalArgumentException("statement amount is invalid");return v.setScale(2);}
 public String getId(){return id;} public String getOwnerId(){return ownerId;} public String getCreditCardId(){return creditCardId;} public LocalDate getCycleStart(){return cycleStart;} public LocalDate getCycleEnd(){return cycleEnd;} public Instant getClosingAt(){return closingAt;} public LocalDate getDueDate(){return dueDate;} public CreditCardStatementStatus getStatus(){return status;} public BigDecimal getTotalAmount(){return totalAmount;} public BigDecimal getPaidAmount(){return paidAmount;} public BigDecimal getOutstandingAmount(){return outstandingAmount;} public Instant getClosedAt(){return closedAt;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;} public Long getVersion(){return version;}
}
