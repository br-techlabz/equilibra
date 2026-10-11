package br.com.equilibra.creditcard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="credit_card_installments", uniqueConstraints=@UniqueConstraint(name="uq_installment_purchase_sequence", columnNames={"purchase_id","sequence_number"}))
public class Installment {
 @Id @Column(length=36,nullable=false,updatable=false) private String id;
 @Column(name="owner_id",length=36,nullable=false,updatable=false) private String ownerId;
 @Column(name="purchase_id",length=36,nullable=false,updatable=false) private String purchaseId;
 @Column(name="credit_card_id",length=36,nullable=false,updatable=false) private String creditCardId;
 @Column(name="sequence_number",nullable=false) private Integer sequenceNumber;
 @Column(name="total_installments",nullable=false) private Integer totalInstallments;
 @Column(name="principal_amount",precision=19,scale=2,nullable=false) private BigDecimal principalAmount;
 @Column(name="cycle_month",nullable=false) private LocalDate cycleMonth;
 @Column(name="statement_id",length=36) private String statementId;
 protected Installment(){}
 public Installment(String ownerId,String purchaseId,String creditCardId,int sequenceNumber,int totalInstallments,BigDecimal principalAmount,LocalDate cycleMonth){this.id=UUID.randomUUID().toString();this.ownerId=ownerId;this.purchaseId=purchaseId;this.creditCardId=creditCardId;this.sequenceNumber=sequenceNumber;this.totalInstallments=totalInstallments;this.principalAmount=principalAmount;this.cycleMonth=cycleMonth;}
 public void assignStatement(String statementId){if(this.statementId!=null&&!this.statementId.equals(statementId))throw new IllegalStateException("installment already belongs to a statement");this.statementId=statementId;}
 public String getId(){return id;} public String getOwnerId(){return ownerId;} public String getPurchaseId(){return purchaseId;} public String getCreditCardId(){return creditCardId;} public Integer getSequenceNumber(){return sequenceNumber;} public Integer getTotalInstallments(){return totalInstallments;} public BigDecimal getPrincipalAmount(){return principalAmount;} public LocalDate getCycleMonth(){return cycleMonth;} public String getStatementId(){return statementId;}
}
