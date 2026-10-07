package br.com.equilibra.transaction.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateExpenseRequest(
    @NotBlank @Size(max = 255) String description,
    @NotNull Instant occurredAt,
    @NotBlank String accountId,
    @NotNull BigDecimal amount,
    @NotBlank String categoryId,
    @Size(max = 4000) String notes,
    java.util.List<String> tagIds
) {}
