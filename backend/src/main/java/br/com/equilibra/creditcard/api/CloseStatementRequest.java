package br.com.equilibra.creditcard.api;
import jakarta.validation.constraints.NotNull; import java.time.LocalDate;
public record CloseStatementRequest(@NotNull LocalDate cycleMonth) {}
