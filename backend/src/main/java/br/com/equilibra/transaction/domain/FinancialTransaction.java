package br.com.equilibra.transaction.domain;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.category.domain.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {

    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final int NOTES_MAX_LENGTH = 4000;
    private static final int MONEY_SCALE = 2;

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "owner_id", length = 36, nullable = false, updatable = false)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TransactionStatus status = TransactionStatus.ACTIVE;

    @Column(name = "description", length = DESCRIPTION_MAX_LENGTH, nullable = false)
    private String description;

    @Column(name = "amount", precision = 19, scale = MONEY_SCALE, nullable = false)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "category_id", length = 36, nullable = false, updatable = false)
    private String categoryId;

    @Column(name = "source_account_id", length = 36, updatable = false)
    private String sourceAccountId;

    @Column(name = "destination_account_id", length = 36, updatable = false)
    private String destinationAccountId;

    @Column(name = "notes", length = NOTES_MAX_LENGTH)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Version
    private Long version;

    protected FinancialTransaction() {
        // Construtor protegido para JPA.
    }

    private FinancialTransaction(
        String ownerId,
        TransactionType type,
        String description,
        BigDecimal amount,
        Instant occurredAt,
        String categoryId,
        String sourceAccountId,
        String destinationAccountId,
        String notes
    ) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = requireText(ownerId, "ownerId");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.description = normalizeDescription(description);
        this.amount = validateAmount(amount);
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        this.categoryId = requireUuid(categoryId, "categoryId");
        this.sourceAccountId = nullableUuid(sourceAccountId, "sourceAccountId");
        this.destinationAccountId = nullableUuid(destinationAccountId, "destinationAccountId");
        this.notes = normalizeNotes(notes);
        this.status = TransactionStatus.ACTIVE;
        validateShape();
    }

    public static FinancialTransaction expense(String ownerId, String description, BigDecimal amount,
                                               Instant occurredAt, String categoryId, String sourceAccountId, String notes) {
        return new FinancialTransaction(ownerId, TransactionType.EXPENSE, description, amount, occurredAt,
            categoryId, sourceAccountId, null, notes);
    }

    public static FinancialTransaction income(String ownerId, String description, BigDecimal amount,
                                              Instant occurredAt, String categoryId, String destinationAccountId, String notes) {
        return new FinancialTransaction(ownerId, TransactionType.INCOME, description, amount, occurredAt,
            categoryId, null, destinationAccountId, notes);
    }

    public static FinancialTransaction transfer(String ownerId, String description, BigDecimal amount,
                                                Instant occurredAt, String categoryId, String sourceAccountId,
                                                String destinationAccountId, String notes) {
        return new FinancialTransaction(ownerId, TransactionType.TRANSFER, description, amount, occurredAt,
            categoryId, sourceAccountId, destinationAccountId, notes);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == TransactionStatus.CANCELLED) {
            throw new IllegalStateException("transaction is already cancelled");
        }
        status = TransactionStatus.CANCELLED;
        cancelledAt = Instant.now();
    }

    public void changeDescription(String description) { ensureActive(); this.description = normalizeDescription(description); }
    public void changeAmount(BigDecimal amount) { ensureActive(); this.amount = validateAmount(amount); }
    public void changeOccurredAt(Instant occurredAt) { ensureActive(); this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null"); }
    public void changeNotes(String notes) { ensureActive(); this.notes = normalizeNotes(notes); }
    public void changeSourceAccount(String sourceAccountId) { ensureActive(); this.sourceAccountId = nullableUuid(sourceAccountId, "sourceAccountId"); validateShape(); }
    public void changeCategory(String categoryId) { ensureActive(); this.categoryId = requireUuid(categoryId, "categoryId"); }

    private void ensureActive() {
        if (status != TransactionStatus.ACTIVE) throw new IllegalStateException("cancelled transaction is immutable");
    }

    private void validateShape() {
        boolean source = sourceAccountId != null;
        boolean destination = destinationAccountId != null;
        switch (type) {
            case EXPENSE -> { if (!source || destination) throw new IllegalArgumentException("expense requires only source account"); }
            case INCOME -> { if (source || !destination) throw new IllegalArgumentException("income requires only destination account"); }
            case TRANSFER -> {
                if (!source || !destination) throw new IllegalArgumentException("transfer requires source and destination accounts");
                if (sourceAccountId.equals(destinationAccountId)) throw new IllegalArgumentException("transfer accounts must differ");
            }
        }
    }

    private static String normalizeDescription(String value) {
        String result = requireText(value, "description");
        if (result.length() > DESCRIPTION_MAX_LENGTH) throw new IllegalArgumentException("description is too long");
        return result;
    }

    private static String normalizeNotes(String value) {
        if (value == null) return null;
        String result = value.trim();
        if (result.length() > NOTES_MAX_LENGTH) throw new IllegalArgumentException("notes are too long");
        return result.isBlank() ? null : result;
    }

    private static BigDecimal validateAmount(BigDecimal value) {
        Objects.requireNonNull(value, "amount must not be null");
        if (value.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        if (value.scale() > MONEY_SCALE) throw new IllegalArgumentException("amount must have at most 2 decimal places");
        return value.setScale(MONEY_SCALE);
    }

    private static String requireUuid(String value, String field) {
        String result = requireText(value, field);
        UUID.fromString(result);
        return result;
    }

    private static String nullableUuid(String value, String field) {
        return value == null ? null : requireUuid(value, field);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getCategoryId() { return categoryId; }
    public String getSourceAccountId() { return sourceAccountId; }
    public String getDestinationAccountId() { return destinationAccountId; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
    public Long getVersion() { return version; }
}
