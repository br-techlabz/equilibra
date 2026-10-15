package br.com.equilibra.account.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReconcileAccountRequest(
    @NotNull LocalDate reconciliationDate,
    @NotNull @PositiveOrZero BigDecimal informedBalance
) {
}