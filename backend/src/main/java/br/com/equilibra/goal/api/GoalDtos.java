package br.com.equilibra.goal.api;
import java.math.*; import java.time.*; import br.com.equilibra.goal.domain.FinancialGoalStatus;
public final class GoalDtos {private GoalDtos(){} public record Create(String name,String description,BigDecimal targetAmount,LocalDate targetDate){} public record Update(String name,String description,BigDecimal targetAmount,LocalDate targetDate){} public record Contribution(BigDecimal amount,Instant occurredAt,String note){} public record Response(String id,String name,String description,BigDecimal targetAmount,LocalDate targetDate,FinancialGoalStatus status,BigDecimal progressAmount,BigDecimal remainingAmount,BigDecimal progressPercentage,Instant createdAt,Instant updatedAt){}
}
