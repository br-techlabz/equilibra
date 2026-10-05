package br.com.equilibra.account.domain;

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
@Table(name = "asset_accounts")
public class AssetAccount {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int MONEY_SCALE = 2;

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "owner_id", length = 36, nullable = false, updatable = false)
    private String ownerId;

    @Column(name = "name", length = NAME_MAX_LENGTH, nullable = false)
    private String name;

    @Column(name = "normalized_name", length = NAME_MAX_LENGTH, nullable = false)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private AssetAccountType type;

    @Column(name = "initial_balance", precision = 19, scale = MONEY_SCALE, nullable = false)
    private BigDecimal initialBalance;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected AssetAccount() {
        // Construtor protegido para JPA.
    }

    public AssetAccount(String ownerId, String name, AssetAccountType type, BigDecimal initialBalance) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = requireText(ownerId, "ownerId");
        this.name = normalizeDisplayName(name);
        this.normalizedName = normalizeName(name);
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.initialBalance = validateMoney(initialBalance);
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.name = normalizeDisplayName(this.name);
        this.normalizedName = normalizeName(this.name);
        this.initialBalance = validateMoney(this.initialBalance);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        this.name = normalizeDisplayName(this.name);
        this.normalizedName = normalizeName(this.name);
        this.initialBalance = validateMoney(this.initialBalance);
    }

    public static String normalizeName(String name) {
        String normalized = normalizeDisplayName(name);
        return normalized.toLowerCase();
    }

    private static String normalizeDisplayName(String name) {
        String normalized = requireText(name, "name");
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("name must have at most " + NAME_MAX_LENGTH + " characters");
        }
        return normalized;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static BigDecimal validateMoney(BigDecimal value) {
        Objects.requireNonNull(value, "initialBalance must not be null");
        if (value.scale() > MONEY_SCALE) {
            throw new IllegalArgumentException("initialBalance must have at most 2 decimal places");
        }
        return value.setScale(MONEY_SCALE);
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public AssetAccountType getType() { return type; }
    public BigDecimal getInitialBalance() { return initialBalance; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    public void rename(String name) {
        this.name = normalizeDisplayName(name);
        this.normalizedName = normalizeName(name);
    }

    public void changeType(AssetAccountType type) {
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public void changeInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = validateMoney(initialBalance);
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AssetAccount that = (AssetAccount) other;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "AssetAccount{" +
            "id='" + id + '\'' +
            ", ownerId='" + ownerId + '\'' +
            ", name='" + name + '\'' +
            ", type=" + type +
            ", initialBalance=" + initialBalance +
            ", active=" + active +
            '}';
    }
}
