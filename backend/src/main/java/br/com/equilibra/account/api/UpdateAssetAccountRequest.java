package br.com.equilibra.account.api;

import br.com.equilibra.account.domain.AssetAccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateAssetAccountRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull AssetAccountType type,
    @NotNull BigDecimal initialBalance
) {}
