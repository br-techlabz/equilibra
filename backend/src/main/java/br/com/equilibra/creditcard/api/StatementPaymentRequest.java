package br.com.equilibra.creditcard.api;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant;
public record StatementPaymentRequest(@NotBlank String bankAccountId,@NotNull @DecimalMin("0.01") BigDecimal amount,@NotNull Instant paidAt){}
