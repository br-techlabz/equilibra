package br.com.equilibra.account.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReconcileAccountRequest(
    @NotNull LocalDate reconciliationDate,
    @NotNull @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$") String reconciliationTime,
    @NotNull @PositiveOrZero BigDecimal informedBalance
) {
}