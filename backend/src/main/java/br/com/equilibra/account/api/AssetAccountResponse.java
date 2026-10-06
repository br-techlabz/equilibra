package br.com.equilibra.account.api;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.domain.AssetAccountType;
import java.math.BigDecimal;
import java.time.Instant;

public record AssetAccountResponse(String id,String name,AssetAccountType type,BigDecimal initialBalance,BigDecimal currentBalance,boolean active,Instant createdAt,Instant updatedAt) {
    public static AssetAccountResponse from(AssetAccount a){return from(a,a.getInitialBalance());}
    public static AssetAccountResponse from(AssetAccount a,BigDecimal balance){return new AssetAccountResponse(a.getId(),a.getName(),a.getType(),a.getInitialBalance(),balance,a.isActive(),a.getCreatedAt(),a.getUpdatedAt());}
}
