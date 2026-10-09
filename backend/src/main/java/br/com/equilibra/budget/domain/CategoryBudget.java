package br.com.equilibra.budget.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="category_budgets", uniqueConstraints=@UniqueConstraint(name="uq_category_budgets_owner_category_month", columnNames={"owner_id","category_id","year","month"}))
public class CategoryBudget {
 @Id @Column(length=36,nullable=false,updatable=false) private String id;
 @Column(name="owner_id",length=36,nullable=false,updatable=false) private String ownerId;
 @Column(name="category_id",length=36,nullable=false,updatable=false) private String categoryId;
 @Column(nullable=false) private short year;
 @Column(nullable=false) private byte month;
 @Column(name="planned_amount",precision=19,scale=2,nullable=false) private BigDecimal plannedAmount;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt;
 @Version private Long version;
 protected CategoryBudget() {}
 public CategoryBudget(String ownerId,String categoryId,int year,int month,BigDecimal amount){this.id=UUID.randomUUID().toString();this.ownerId=ownerId;this.categoryId=categoryId;changeCompetence(year,month);changeAmount(amount);}
 public void changeAmount(BigDecimal amount){if(amount==null||amount.signum()<0||amount.scale()>2)throw new IllegalArgumentException("plannedAmount must be non-negative with at most 2 decimal places");this.plannedAmount=amount.setScale(2);}
 private void changeCompetence(int year,int month){if(year<2000||year>9999||month<1||month>12)throw new IllegalArgumentException("invalid monthly competence");this.year=(short)year;this.month=(byte)month;}
 @PrePersist protected void create(){Instant now=Instant.now();createdAt=now;updatedAt=now;} @PreUpdate protected void update(){updatedAt=Instant.now();}
 public String getId(){return id;} public String getOwnerId(){return ownerId;} public String getCategoryId(){return categoryId;} public short getYear(){return year;} public byte getMonth(){return month;} public BigDecimal getPlannedAmount(){return plannedAmount;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
