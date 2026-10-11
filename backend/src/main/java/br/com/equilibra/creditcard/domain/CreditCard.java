package br.com.equilibra.creditcard.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "credit_cards")
public class CreditCard {
    private static final int MONEY_SCALE = 2;
    private static final String DEFAULT_TIMEZONE = "America/Sao_Paulo";

    @Id @Column(length = 36, nullable = false, updatable = false)
    private String id;
    @Column(name = "owner_id", length = 36, nullable = false, updatable = false)
    private String ownerId;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 100) private String institution;
    @Column(length = 100) private String nickname;
    @Column(name = "total_limit", precision = 19, scale = 2, nullable = false) private BigDecimal totalLimit;
    @Column(name = "closing_day", nullable = false) private Integer closingDay;
    @Column(name = "due_day", nullable = false) private Integer dueDay;
    @Column(nullable = false, length = 64) private String timezone;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CreditCardStatus status = CreditCardStatus.ACTIVE;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version private Long version;

    protected CreditCard() {}

    public CreditCard(String ownerId, String name, String institution, String nickname, BigDecimal totalLimit,
                      Integer closingDay, Integer dueDay, String timezone) {
        this.id = UUID.randomUUID().toString();
        this.ownerId = requireText(ownerId, "ownerId");
        changeDetails(name, institution, nickname, totalLimit, closingDay, dueDay, timezone);
    }

    @PrePersist protected void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate protected void onUpdate() { updatedAt = Instant.now(); }

    public void changeDetails(String name, String institution, String nickname, BigDecimal totalLimit,
                              Integer closingDay, Integer dueDay, String timezone) {
        this.name = text(name, "name", 100);
        this.institution = text(institution, "institution", 100);
        this.nickname = optionalText(nickname, 100);
        this.totalLimit = money(totalLimit);
        this.closingDay = day(closingDay, "closingDay");
        this.dueDay = day(dueDay, "dueDay");
        this.timezone = validTimezone(timezone);
    }
    public void deactivate() { status = CreditCardStatus.INACTIVE; }
    public void activate() { status = CreditCardStatus.ACTIVE; }

    private static String text(String value, String field, int max) { String v = requireText(value, field); if (v.length() > max) throw new IllegalArgumentException(field + " is too long"); return v; }
    private static String optionalText(String value, int max) { if (value == null || value.trim().isBlank()) return null; String v = value.trim(); if (v.length() > max) throw new IllegalArgumentException("nickname is too long"); return v; }
    private static String requireText(String value, String field) { if (value == null || value.trim().isBlank()) throw new IllegalArgumentException(field + " must not be blank"); return value.trim(); }
    private static BigDecimal money(BigDecimal value) { Objects.requireNonNull(value, "totalLimit must not be null"); if (value.signum() <= 0 || value.scale() > MONEY_SCALE) throw new IllegalArgumentException("totalLimit must be positive with at most 2 decimal places"); return value.setScale(MONEY_SCALE); }
    private static int day(Integer value, String field) { if (value == null || value < 1 || value > 31) throw new IllegalArgumentException(field + " must be between 1 and 31"); return value; }
    private static String validTimezone(String value) { String v = value == null || value.isBlank() ? DEFAULT_TIMEZONE : value.trim(); try { ZoneId.of(v); return v; } catch (Exception e) { throw new IllegalArgumentException("timezone must be a valid IANA zone ID", e); } }

    public String getId() { return id; } public String getOwnerId() { return ownerId; } public String getName() { return name; }
    public String getInstitution() { return institution; } public String getNickname() { return nickname; } public BigDecimal getTotalLimit() { return totalLimit; }
    public Integer getClosingDay() { return closingDay; } public Integer getDueDay() { return dueDay; } public String getTimezone() { return timezone; }
    public CreditCardStatus getStatus() { return status; } public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; } public Long getVersion() { return version; }
}
