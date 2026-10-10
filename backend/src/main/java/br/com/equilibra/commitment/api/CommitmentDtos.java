package br.com.equilibra.commitment.api;
import br.com.equilibra.commitment.domain.*; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*;
public final class CommitmentDtos { private CommitmentDtos(){}
 public record Create(@NotNull CommitmentType type,@NotBlank @Size(max=255) String description,@NotNull @DecimalMin("0.01") BigDecimal plannedAmount,@NotNull LocalDate dueDate,@NotBlank String accountId,@NotBlank String categoryId,String recurrenceRuleId){}
 public record Update(@NotBlank @Size(max=255) String description,@NotNull @DecimalMin("0.01") BigDecimal plannedAmount,@NotNull LocalDate dueDate,@NotBlank String accountId,@NotBlank String categoryId){}
 public record Generate(@NotNull LocalDate from,@NotNull LocalDate to){}
 public record Settle(@NotNull Instant occurredAt,@NotNull @DecimalMin("0.01") BigDecimal amount){}
 public record Filter(CommitmentType type,CommitmentStatus status,LocalDate from,LocalDate to,String accountId,String categoryId,String recurrenceRuleId){}
 public record Response(String id,CommitmentType type,String description,BigDecimal plannedAmount,LocalDate dueDate,String accountId,String categoryId,String recurrenceRuleId,CommitmentStatus status,String financialTransactionId,Instant settledAt,boolean overdue,Instant createdAt,Instant updatedAt){}
}
