package br.com.equilibra.tag.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tags")
public class Tag {
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
    @Column(name = "active", nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    private Long version;

    protected Tag() {}

    public Tag(String ownerId, String name) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = requireText(ownerId, "ownerId");
        rename(name);
    }

    @PrePersist
    protected void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public static String normalizeName(String name) { return validateName(name).toLowerCase(Locale.ROOT); }

    private static String validateName(String value) {
        String result = requireText(value, "name");
        if (result.length() > NAME_MAX_LENGTH) throw new IllegalArgumentException("name must have at most 100 characters");
        return result;
    }
    private static String requireText(String value, String field) {
        if (value == null || value.trim().isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }

    public void rename(String value) { name = validateName(value); normalizedName = normalizeName(name); }
    public void deactivate() { active = false; }
    public void activate() { active = true; }
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    @Override public boolean equals(Object other) { return this == other || other instanceof Tag tag && Objects.equals(id, tag.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
