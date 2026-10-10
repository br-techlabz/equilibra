package br.com.equilibra.commitment.application;

import br.com.equilibra.commitment.domain.*;
import br.com.equilibra.commitment.infrastructure.FinancialCommitmentRepository;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class FinancialCommitmentRollbackTest {
    @Autowired private FinancialCommitmentRepository commitments;
    @Autowired private UserRepository users;
    @Autowired private TransactionTemplate transactions;

    @Test
    void rollsBackCommitmentWhenUnitOfWorkFailsAfterPersisting() {
        User user = users.saveAndFlush(new User("rollback-" + UUID.randomUUID() + "@example.test", "not-a-real-password-hash"));
        assertThatThrownBy(() -> transactions.executeWithoutResult(status -> {
            FinancialCommitment commitment = new FinancialCommitment(
                user.getId(), CommitmentType.EXPENSE, "Rollback", new BigDecimal("10.00"),
                LocalDate.of(2026, 10, 10), UUID.randomUUID().toString(), UUID.randomUUID().toString(), null);
            commitments.saveAndFlush(commitment);
            throw new IllegalStateException("simulated failure after persist");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(commitments.findAll()).noneMatch(item -> "Rollback".equals(item.getDescription()));
    }
}
