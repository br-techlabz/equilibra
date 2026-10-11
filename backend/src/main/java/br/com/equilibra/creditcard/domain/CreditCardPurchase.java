package br.com.equilibra.creditcard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "credit_card_purchases")
public class CreditCardPurchase {
    @Id @Column(length=36, nullable=false, updatable=false) private String id;
    @Column(name="owner_id", length=36, nullable=false, updatable=false) private String ownerId;
    @Column(name="credit_card_id", length=36, nullable=false, updatable=false) private String creditCardId;
    @Column(name="category_id", length=36, nullable=false, updatable=false) private String categoryId;
    @Column(nullable=false, length=255) private String description;
    @Column(name="total_amount", precision=19, scale=2, nullable=false) private BigDecimal totalAmount;
    @Column(name="purchase_date", nullable=false) private LocalDate purchaseDate;
    @Column(name="purchase_at") private java.time.Instant purchaseAt;
    @Column(name="installment_count", nullable=false) private Integer installmentCount;
    @Column(name="cycle_month", nullable=false) private LocalDate cycleMonth;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private CreditCardPurchaseStatus status=CreditCardPurchaseStatus.ACTIVE;
    @Column(name="created_at", nullable=false, updatable=false) private java.time.Instant createdAt;
    @Column(name="updated_at", nullable=false) private java.time.Instant updatedAt;
    @Version private Long version;

    protected CreditCardPurchase() {}
    public CreditCardPurchase(String ownerId, String creditCardId, String categoryId, String description, BigDecimal totalAmount, LocalDate purchaseDate, int installmentCount, LocalDate cycleMonth) {
        this.id=UUID.randomUUID().toString(); this.ownerId=ownerId; this.creditCardId=creditCardId; this.categoryId=categoryId; this.description=description.trim(); this.totalAmount=validateMoney(totalAmount); this.purchaseDate=purchaseDate; if(installmentCount<1) throw new IllegalArgumentException("installmentCount must be positive"); this.installmentCount=installmentCount; this.cycleMonth=cycleMonth;
    }
    @PrePersist void create(){createdAt=java.time.Instant.now();updatedAt=createdAt;} @PreUpdate void update(){updatedAt=java.time.Instant.now();}
    public void setPurchaseAt(java.time.Instant value){this.purchaseAt=value;}
    public void cancel(){if(status!=CreditCardPurchaseStatus.ACTIVE)throw new IllegalStateException("purchase is not active");status=CreditCardPurchaseStatus.CANCELLED;}
    public void reverse(){if(status!=CreditCardPurchaseStatus.ACTIVE)throw new IllegalStateException("purchase is not active");status=CreditCardPurchaseStatus.REVERSED;}
    private static BigDecimal validateMoney(BigDecimal v){if(v==null||v.signum()<=0||v.scale()>2)throw new IllegalArgumentException("totalAmount must be positive with at most 2 decimals");return v.setScale(2);}
    public String getId(){return id;} public java.time.Instant getPurchaseAt(){return purchaseAt;} public String getOwnerId(){return ownerId;} public String getCreditCardId(){return creditCardId;} public String getCategoryId(){return categoryId;} public String getDescription(){return description;} public BigDecimal getTotalAmount(){return totalAmount;} public LocalDate getPurchaseDate(){return purchaseDate;} public Integer getInstallmentCount(){return installmentCount;} public LocalDate getCycleMonth(){return cycleMonth;} public CreditCardPurchaseStatus getStatus(){return status;} public java.time.Instant getCreatedAt(){return createdAt;} public java.time.Instant getUpdatedAt(){return updatedAt;} public Long getVersion(){return version;}
}
