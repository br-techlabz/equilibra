package br.com.equilibra.transaction.api;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant;
public record CreateTransferRequest(@NotBlank @Size(max=255) String description,@NotNull Instant occurredAt,@NotBlank String sourceAccountId,@NotBlank String destinationAccountId,@NotNull BigDecimal amount,@NotBlank String categoryId,@Size(max=4000) String notes,java.util.List<String> tagIds) {}
