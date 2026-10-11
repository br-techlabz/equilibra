package br.com.equilibra.creditcard.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCreditCardRequest(
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Size(max = 100) String institution,
    @Size(max = 100) String nickname,
    @NotNull @DecimalMin(value = "0.01") BigDecimal totalLimit,
    @NotNull @Min(1) @Max(31) Integer closingDay,
    @NotNull @Min(1) @Max(31) Integer dueDay,
    @Size(max = 64) String timezone
) {}
