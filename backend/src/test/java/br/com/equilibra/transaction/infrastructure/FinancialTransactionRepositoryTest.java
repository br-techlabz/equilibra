package br.com.equilibra.transaction.infrastructure;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.domain.AssetAccountType;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.flywaydb.core.Flyway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "logging.level.org.hibernate.SQL=OFF",
    "logging.level.org.hibernate.orm.jdbc.bind=OFF"
})
@Testcontainers
@ActiveProfiles("test")
class FinancialTransactionRepositoryTest {

    @Autowired private FinancialTransactionRepository transactions;
    @Autowired private UserRepository users;
    @Autowired private AssetAccountRepository accounts;
    @Autowired private CategoryRepository categories;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private javax.sql.DataSource dataSource;
    @Autowired private EntityManager entityManager;

    private String owner;
    private String otherOwner;
    private AssetAccount source;
    private AssetAccount destination;
    private Category expenseCategory;
    private Category incomeCategory;
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
        transaction = new TransactionTemplate(transactionManager);
        String hash = passwordEncoder.encode(UUID.randomUUID().toString());
        owner = users.saveAndFlush(new User(UUID.randomUUID() + "@transaction.test", hash)).getId();
        otherOwner = users.saveAndFlush(new User(UUID.randomUUID() + "@transaction.test", hash)).getId();
        source = accounts.saveAndFlush(new AssetAccount(owner, "Origem", AssetAccountType.CASH, new BigDecimal("100.00")));
        destination = accounts.saveAndFlush(new AssetAccount(owner, "Destino", AssetAccountType.CASH, new BigDecimal("0.00")));
        expenseCategory = categories.saveAndFlush(new Category(owner, "Alimentação", CategoryApplicability.EXPENSE));
        incomeCategory = categories.saveAndFlush(new Category(owner, "Salário", CategoryApplicability.INCOME));
    }

    @Test
    void shouldPersistReloadAndQueryTypesAndPeriod() {
        FinancialTransaction expense = transactions.saveAndFlush(FinancialTransaction.expense(
            owner, "Mercado", new BigDecimal("10.10"), Instant.parse("2026-10-01T10:00:00Z"), expenseCategory.getId(), source.getId(), null));
        FinancialTransaction income = transactions.saveAndFlush(FinancialTransaction.income(
            owner, "Salário", new BigDecimal("20.20"), Instant.parse("2026-10-02T10:00:00Z"), incomeCategory.getId(), destination.getId(), null));
        entityManager.clear();
        FinancialTransaction found = transactions.findByIdAndOwnerId(expense.getId(), owner).orElseThrow();
        assertThat(found.getAmount()).isEqualByComparingTo("10.10");
        assertThat(found.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(found.getStatus()).isEqualTo(TransactionStatus.ACTIVE);
        assertThat(transactions.findAllByOwnerIdOrderByOccurredAtDesc(owner)).extracting(FinancialTransaction::getId)
            .containsExactly(income.getId(), expense.getId());
        assertThat(transactions.findAllByOwnerIdAndTypeAndOccurredAtBetweenOrderByOccurredAtDesc(
            owner, TransactionType.INCOME, Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-10-03T00:00:00Z")))
            .extracting(FinancialTransaction::getId).containsExactly(income.getId());
    }

    @Test
    void shouldFilterActiveStatusAndAccountAffectingQueriesByOwner() {
        FinancialTransaction active = transactions.saveAndFlush(FinancialTransaction.expense(
            owner, "Compra", new BigDecimal("1.00"), Instant.now(), expenseCategory.getId(), source.getId(), null));
        FinancialTransaction cancelled = transactions.saveAndFlush(FinancialTransaction.expense(
            owner, "Cancelada", new BigDecimal("2.00"), Instant.now(), expenseCategory.getId(), source.getId(), null));
        cancelled.cancel();
        transactions.saveAndFlush(cancelled);
        FinancialTransaction transfer = transactions.saveAndFlush(FinancialTransaction.transfer(
            owner, "Movimentação", new BigDecimal("3.00"), Instant.now(), expenseCategory.getId(), source.getId(), destination.getId(), null));
        assertThat(transactions.findAllByOwnerIdAndStatusOrderByOccurredAtDesc(owner, TransactionStatus.ACTIVE))
            .extracting(FinancialTransaction::getId).contains(active.getId(), transfer.getId()).doesNotContain(cancelled.getId());
        assertThat(transactions.findAllByOwnerIdAndSourceAccountIdOrOwnerIdAndDestinationAccountIdOrderByOccurredAtDesc(
            owner, source.getId(), owner, source.getId()))
            .extracting(FinancialTransaction::getId).contains(active.getId(), cancelled.getId(), transfer.getId());
        assertThat(transactions.findByIdAndOwnerId(active.getId(), otherOwner)).isEmpty();
    }

    @Test
    void shouldKeepCancelledTimestampAndAllowHistoricalAccountStatus() {
        source.deactivate();
        accounts.saveAndFlush(source);
        FinancialTransaction saved = transactions.saveAndFlush(FinancialTransaction.expense(
            owner, "Histórico", new BigDecimal("1.00"), Instant.now(), expenseCategory.getId(), source.getId(), null));
        saved.cancel();
        transactions.saveAndFlush(saved);
        FinancialTransaction reloaded = transactions.findByIdAndOwnerId(saved.getId(), owner).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(TransactionStatus.CANCELLED);
        assertThat(reloaded.getCancelledAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidForeignKeysAndDatabaseShapes() {
        assertThatThrownBy(() -> transactions.saveAndFlush(FinancialTransaction.expense(
            owner, "FK", new BigDecimal("1.00"), Instant.now(), UUID.randomUUID().toString(), source.getId(), null)))
            .isInstanceOf(DataIntegrityViolationException.class);
        FinancialTransaction transfer = FinancialTransaction.transfer(
            owner, "shape", new BigDecimal("1.00"), Instant.now(), expenseCategory.getId(), source.getId(), destination.getId(), null);
        assertThat(transactions.saveAndFlush(transfer).getType()).isEqualTo(TransactionType.TRANSFER);
    }
}
