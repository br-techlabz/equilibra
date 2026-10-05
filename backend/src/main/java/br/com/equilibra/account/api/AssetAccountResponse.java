package br.com.equilibra.account.api;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.domain.AssetAccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AssetAccountResponse(
    String id,
    String name,
    AssetAccountType type,
    BigDecimal initialBalance,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    public static AssetAccountResponse from(AssetAccount account) {
        return new AssetAccountResponse(
            account.getId(),
            account.getName(),
            account.getType(),
            account.getInitialBalance(),
            account.isActive(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}
