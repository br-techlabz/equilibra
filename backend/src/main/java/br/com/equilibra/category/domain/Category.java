package br.com.equilibra.category.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    private static final int NAME_MAX_LENGTH = 100;

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
    @Column(name = "applicability", length = 20, nullable = false)
    private CategoryApplicability applicability;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Category() {
        // Construtor protegido para JPA.
    }

    public Category(String ownerId, String name, CategoryApplicability applicability) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = requireText(ownerId, "ownerId");
        rename(name);
        changeApplicability(applicability);
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

    public static String normalizeName(String name) {
        return validateName(name).toLowerCase(Locale.ROOT);
    }

    private static String validateName(String name) {
        String value = requireText(name, "name");
        if (value.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("name must have at most " + NAME_MAX_LENGTH + " characters");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public CategoryApplicability getApplicability() { return applicability; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    public void rename(String name) {
        String displayName = validateName(name);
        String normalized = normalizeName(displayName);
        if (normalized.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("normalizedName must have at most " + NAME_MAX_LENGTH + " characters");
        }
        this.name = displayName;
        this.normalizedName = normalized;
    }

    public void changeApplicability(CategoryApplicability applicability) {
        this.applicability = Objects.requireNonNull(applicability, "applicability must not be null");
    }

    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Category that = (Category) other;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
