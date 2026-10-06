package br.com.equilibra.transaction.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinancialTransactionTest {

    private final String owner = UUID.randomUUID().toString();
    private final String category = UUID.randomUUID().toString();
    private final String source = UUID.randomUUID().toString();
    private final String destination = UUID.randomUUID().toString();
    private final Instant occurredAt = Instant.parse("2026-10-06T12:00:00Z");

    @Test
    void shouldCreateExpenseWithOnlySourceAccount() {
        FinancialTransaction transaction = FinancialTransaction.expense(owner, " Mercado ", new BigDecimal("100.10"), occurredAt, category, source, " nota ");
        assertThat(transaction.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.ACTIVE);
        assertThat(transaction.getDescription()).isEqualTo("Mercado");
        assertThat(transaction.getAmount()).isEqualByComparingTo("100.10");
        assertThat(transaction.getSourceAccountId()).isEqualTo(source);
        assertThat(transaction.getDestinationAccountId()).isNull();
        assertThat(transaction.getNotes()).isEqualTo("nota");
    }

    @Test
    void shouldCreateIncomeWithOnlyDestinationAccount() {
        FinancialTransaction transaction = FinancialTransaction.income(owner, " Salário ", new BigDecimal("200.20"), occurredAt, category, destination, null);
        assertThat(transaction.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(transaction.getSourceAccountId()).isNull();
        assertThat(transaction.getDestinationAccountId()).isEqualTo(destination);
    }

    @Test
    void shouldCreateTransferWithDifferentAccounts() {
        FinancialTransaction transaction = FinancialTransaction.transfer(owner, "Transferência", new BigDecimal("50.00"), occurredAt, category, source, destination, null);
        assertThat(transaction.getType()).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    void shouldRejectInvalidAmounts() {
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, "x", BigDecimal.ZERO, occurredAt, category, source, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, "x", new BigDecimal("-1.00"), occurredAt, category, source, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, "x", new BigDecimal("1.001"), occurredAt, category, source, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidDescriptionsAndNotes() {
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, " ", new BigDecimal("1.00"), occurredAt, category, source, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, "x".repeat(256), new BigDecimal("1.00"), occurredAt, category, source, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, "x", new BigDecimal("1.00"), occurredAt, category, source, "n".repeat(4001))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidAccountShapes() {
        assertThatThrownBy(() -> FinancialTransaction.expense(owner, "x", new BigDecimal("1.00"), occurredAt, category, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FinancialTransaction.income(owner, "x", new BigDecimal("1.00"), occurredAt, category, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FinancialTransaction.transfer(owner, "x", new BigDecimal("1.00"), occurredAt, category, source, source, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCancelOnlyOnceAndMakeTransactionImmutable() {
        FinancialTransaction transaction = FinancialTransaction.expense(owner, "x", new BigDecimal("1.00"), occurredAt, category, source, null);
        transaction.cancel();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.CANCELLED);
        assertThat(transaction.getCancelledAt()).isNotNull();
        assertThatThrownBy(transaction::cancel).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> transaction.changeAmount(new BigDecimal("2.00"))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldAllowEditingActiveFields() {
        FinancialTransaction transaction = FinancialTransaction.expense(owner, "x", new BigDecimal("1.00"), occurredAt, category, source, null);
        transaction.changeDescription("y");
        transaction.changeAmount(new BigDecimal("2.00"));
        transaction.changeOccurredAt(Instant.parse("2026-10-07T12:00:00Z"));
        transaction.changeNotes("notes");
        assertThat(transaction.getDescription()).isEqualTo("y");
        assertThat(transaction.getAmount()).isEqualByComparingTo("2.00");
        assertThat(transaction.getNotes()).isEqualTo("notes");
    }
}
