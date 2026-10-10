package br.com.equilibra.commitment.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FinancialCommitmentTest {
 private final String owner=UUID.randomUUID().toString(); private final String account=UUID.randomUUID().toString(); private final String category=UUID.randomUUID().toString();
 private FinancialCommitment commitment(){return new FinancialCommitment(owner,CommitmentType.EXPENSE,"Internet",new BigDecimal("129.90"),LocalDate.of(2026,10,10),account,category,null);}
 @Test void createsPendingAndDerivesNoFinancialSideEffect(){var c=commitment();assertThat(c.getStatus()).isEqualTo(CommitmentStatus.PENDING);assertThat(c.getPlannedAmount()).isEqualByComparingTo("129.90");assertThat(c.getFinancialTransactionId()).isNull();}
 @Test void cancelsPendingOnly(){var c=commitment();c.cancel();assertThat(c.getStatus()).isEqualTo(CommitmentStatus.CANCELLED);assertThatThrownBy(c::cancel).isInstanceOf(IllegalStateException.class);}
 @Test void settlesOnceAndKeepsPlannedAmount(){var c=commitment();c.settle(UUID.randomUUID().toString(),Instant.parse("2026-10-10T12:00:00Z"));assertThat(c.getStatus()).isEqualTo(CommitmentStatus.SETTLED);assertThat(c.getPlannedAmount()).isEqualByComparingTo("129.90");assertThatThrownBy(()->c.settle(UUID.randomUUID().toString(),Instant.now())).isInstanceOf(IllegalStateException.class);}
 @Test void rejectsInvalidMoneyAndDate(){assertThatThrownBy(()->new FinancialCommitment(owner,CommitmentType.EXPENSE,"x",new BigDecimal("1.001"),LocalDate.now(),account,category,null)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->new FinancialCommitment(owner,CommitmentType.EXPENSE,"x",new BigDecimal("1.00"),null,account,category,null)).isInstanceOf(IllegalArgumentException.class);}
 @Test void editsPendingOnly(){var c=commitment();c.rename("Updated");c.changeAmount(new BigDecimal("100.00"));c.changeDueDate(LocalDate.of(2026,11,10));assertThat(c.getDescription()).isEqualTo("Updated");c.settle(UUID.randomUUID().toString(),Instant.now());assertThatThrownBy(()->c.rename("nope")).isInstanceOf(IllegalStateException.class);}
}
