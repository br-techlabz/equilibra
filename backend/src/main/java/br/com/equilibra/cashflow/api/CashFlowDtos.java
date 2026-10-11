package br.com.equilibra.cashflow.api;
import java.math.BigDecimal; import java.time.*; import java.util.*;
public final class CashFlowDtos { private CashFlowDtos(){}
 public record Response(LocalDate from,LocalDate to,BigDecimal openingBalance,BigDecimal projectedIncome,BigDecimal projectedExpense,BigDecimal projectedChange,BigDecimal closingBalance,BigDecimal minimumBalance,List<Point> series,List<Event> events){}
 public record Point(LocalDate date,BigDecimal balance){}
 public record Event(String commitmentId,LocalDate dueDate,String description,String type,BigDecimal amount,String accountId,String categoryId){}
}
