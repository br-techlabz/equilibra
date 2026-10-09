package br.com.equilibra.budget.api;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant;
public final class BudgetDtos { private BudgetDtos(){}
 public record Create(@NotNull String categoryId,@NotBlank String month,@NotNull @DecimalMin("0.00") @Digits(integer=17,fraction=2) BigDecimal plannedAmount){}
 public record Update(@NotNull @DecimalMin("0.00") @Digits(integer=17,fraction=2) BigDecimal plannedAmount){}
 public record Response(String id,String categoryId,String categoryName,boolean categoryActive,int year,int month,BigDecimal plannedAmount,Instant createdAt,Instant updatedAt){}
}
