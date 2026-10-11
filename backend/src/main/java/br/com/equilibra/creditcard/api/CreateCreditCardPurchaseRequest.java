package br.com.equilibra.creditcard.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCreditCardPurchaseRequest(@NotBlank @Size(max=255) String description,@NotNull LocalDate purchaseDate,@NotNull @DecimalMin("0.01") BigDecimal totalAmount,@NotNull @Min(1) @Max(60) Integer installmentCount,@NotBlank String creditCardId,@NotBlank String categoryId) {}
