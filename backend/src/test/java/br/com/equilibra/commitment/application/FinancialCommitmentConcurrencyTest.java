package br.com.equilibra.commitment.application;

import br.com.equilibra.commitment.domain.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

class FinancialCommitmentConcurrencyTest {
 @Test void twoSettlementAttemptsCanOnlyTransitionOneCommitment(){var c=new FinancialCommitment(UUID.randomUUID().toString(),CommitmentType.EXPENSE,"Concurrent",new BigDecimal("10.00"),LocalDate.of(2026,10,10),UUID.randomUUID().toString(),UUID.randomUUID().toString(),null);var gate=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);Callable<Boolean> operation=()->{gate.await();synchronized(c){if(c.getStatus()!=CommitmentStatus.PENDING)return false;c.settle(UUID.randomUUID().toString(),Instant.parse("2026-10-10T12:00:00Z"));return true;}};try{Future<Boolean> first=pool.submit(operation);Future<Boolean> second=pool.submit(operation);gate.countDown();assertThat(first.get()).isNotEqualTo(second.get());assertThat(c.getStatus()).isEqualTo(CommitmentStatus.SETTLED);}catch(Exception e){throw new AssertionError(e);}finally{pool.shutdownNow();}}
}
