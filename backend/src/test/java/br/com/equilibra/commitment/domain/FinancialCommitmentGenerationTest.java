package br.com.equilibra.commitment.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class FinancialCommitmentGenerationTest {
 @Test void dayThirtyOneCanBeSettledOnLastValidDay(){var c=new FinancialCommitment(UUID.randomUUID().toString(),CommitmentType.EXPENSE,"Internet",new BigDecimal("10.00"),LocalDate.of(2026,2,28),UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString());assertThat(c.getDueDate()).isEqualTo(LocalDate.of(2026,2,28));c.settle(UUID.randomUUID().toString(),Instant.parse("2026-02-28T12:00:00Z"));assertThat(c.getStatus()).isEqualTo(CommitmentStatus.SETTLED);}
 @Test void pendingCommitmentReportsContractualStatesOnly(){var c=new FinancialCommitment(UUID.randomUUID().toString(),CommitmentType.INCOME,"Salary",new BigDecimal("10.00"),LocalDate.of(2026,4,30),UUID.randomUUID().toString(),UUID.randomUUID().toString(),null);assertThat(c.getStatus()).isEqualTo(CommitmentStatus.PENDING);assertThat(c.getFinancialTransactionId()).isNull();}
}
