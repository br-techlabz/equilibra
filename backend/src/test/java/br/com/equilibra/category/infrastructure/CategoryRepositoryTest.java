package br.com.equilibra.category.infrastructure;

import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "logging.level.org.hibernate.SQL=OFF",
    "logging.level.org.hibernate.orm.jdbc.bind=OFF"
})
@Testcontainers
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired private CategoryRepository categories;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private EntityManager entityManager;
    @Autowired private JdbcTemplate jdbc;

    private String ownerA;
    private String ownerB;
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        transaction = new TransactionTemplate(transactionManager);
        String encoded = passwordEncoder.encode(UUID.randomUUID().toString());
        ownerA = users.saveAndFlush(new User(UUID.randomUUID() + "@example.test", encoded)).getId();
        ownerB = users.saveAndFlush(new User(UUID.randomUUID() + "@example.test", encoded)).getId();
    }

    private Category save(String owner, String name, CategoryApplicability applicability) {
        return categories.saveAndFlush(new Category(owner, name, applicability));
    }

    @ParameterizedTest
    @EnumSource(CategoryApplicability.class)
    void shouldPersistAndReloadAllFields(CategoryApplicability applicability) {
        transaction.executeWithoutResult(status -> {
            Category saved = save(ownerA, " Alimentação ", applicability);
            entityManager.clear();
            Category found = categories.findByIdAndOwnerId(saved.getId(), ownerA).orElseThrow();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getOwnerId()).isEqualTo(ownerA);
            assertThat(found.getName()).isEqualTo("Alimentação");
            assertThat(found.getNormalizedName()).isEqualTo("alimentação");
            assertThat(found.getApplicability()).isEqualTo(applicability);
            assertThat(found.isActive()).isTrue();
            assertThat(found.getCreatedAt()).isNotNull();
            assertThat(found.getUpdatedAt()).isEqualTo(found.getCreatedAt());
            assertThat(found.getVersion()).isZero();
            assertThat(jdbc.queryForObject("SELECT applicability FROM categories WHERE id = ?", String.class, found.getId()))
                .isEqualTo(applicability.name());
        });
    }

    @Test
    void shouldUpdateTimestampsAndVersionWhilePreservingOwnerAndCreation() {
        Category saved = save(ownerA, "Outros", CategoryApplicability.EXPENSE);
        Category before = categories.findByIdAndOwnerId(saved.getId(), ownerA).orElseThrow();
        Instant createdAt = before.getCreatedAt();
        before.rename(" Diversos ");
        before.changeApplicability(CategoryApplicability.BOTH);
        before.deactivate();
        categories.saveAndFlush(before);
        Category after = categories.findByIdAndOwnerId(saved.getId(), ownerA).orElseThrow();
        assertThat(after.getCreatedAt()).isEqualTo(createdAt);
        assertThat(after.getUpdatedAt()).isAfter(before.getUpdatedAt());
        assertThat(after.getVersion()).isGreaterThan(before.getVersion());
        assertThat(after.getOwnerId()).isEqualTo(ownerA);
        assertThat(after.getName()).isEqualTo("Diversos");
        assertThat(after.getNormalizedName()).isEqualTo("diversos");
        assertThat(after.getApplicability()).isEqualTo(CategoryApplicability.BOTH);
        assertThat(after.isActive()).isFalse();
        after.activate();
        categories.saveAndFlush(after);
        assertThat(categories.findByIdAndOwnerId(saved.getId(), ownerA).orElseThrow().isActive()).isTrue();
    }

    @Test
    void shouldIsolateLookupAndListsByOwner() {
        Category food = save(ownerA, "Alimentação", CategoryApplicability.EXPENSE);
        Category housing = save(ownerA, "Moradia", CategoryApplicability.EXPENSE);
        save(ownerB, "Salário", CategoryApplicability.INCOME);
        assertThat(categories.findByIdAndOwnerId(food.getId(), ownerA)).isPresent();
        assertThat(categories.findByIdAndOwnerId(food.getId(), ownerB)).isEmpty();
        assertThat(categories.findAllByOwnerIdOrderByNameAsc(ownerA))
            .extracting(Category::getId).containsExactly(food.getId(), housing.getId());
        housing.deactivate();
        categories.saveAndFlush(housing);
        assertThat(categories.findAllByOwnerIdAndActiveTrueOrderByNameAsc(ownerA))
            .extracting(Category::getId).containsExactly(food.getId());
    }

    @Test
    void shouldFindOnlyAvailableCategoriesForEachApplicabilityAndOwner() {
        Category food = save(ownerA, "Alimentação", CategoryApplicability.EXPENSE);
        Category salary = save(ownerA, "Salário", CategoryApplicability.INCOME);
        Category both = save(ownerA, "Outros", CategoryApplicability.BOTH);
        Category inactive = save(ownerA, "Arquivada", CategoryApplicability.BOTH);
        inactive.deactivate();
        categories.saveAndFlush(inactive);
        save(ownerB, "Outra pessoa", CategoryApplicability.BOTH);
        assertThat(categories.findAllByOwnerIdAndActiveTrueAndApplicabilityInOrderByNameAsc(
            ownerA, List.of(CategoryApplicability.EXPENSE, CategoryApplicability.BOTH)))
            .extracting(Category::getId).containsExactly(food.getId(), both.getId());
        assertThat(categories.findAllByOwnerIdAndActiveTrueAndApplicabilityInOrderByNameAsc(
            ownerA, List.of(CategoryApplicability.INCOME, CategoryApplicability.BOTH)))
            .extracting(Category::getId).containsExactly(both.getId(), salary.getId());
    }

    @Test
    void shouldScopeNameExistenceToActiveOwnerAndExcludeCurrentId() {
        Category food = save(ownerA, "Alimentação", CategoryApplicability.EXPENSE);
        assertThat(categories.existsByOwnerIdAndNormalizedNameAndActiveTrue(ownerA, "alimentação")).isTrue();
        assertThat(categories.existsByOwnerIdAndNormalizedNameAndActiveTrue(ownerB, "alimentação")).isFalse();
        assertThat(categories.existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(ownerA, "alimentação", food.getId())).isFalse();
        assertThat(categories.existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(ownerA, "alimentação", UUID.randomUUID().toString())).isTrue();
        food.deactivate();
        categories.saveAndFlush(food);
        assertThat(categories.existsByOwnerIdAndNormalizedNameAndActiveTrue(ownerA, "alimentação")).isFalse();
    }

    @ParameterizedTest
    @EnumSource(CategoryApplicability.class)
    void shouldRejectDuplicateActiveNameRegardlessOfApplicability(CategoryApplicability applicability) {
        save(ownerA, "Alimentação", CategoryApplicability.EXPENSE);
        assertThatThrownBy(() -> save(ownerA, " ALIMENTAÇÃO ", applicability))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFollowAccentInsensitiveDatabaseCollationWithoutChangingDisplayName() {
        Category saved = save(ownerA, "Alimentação", CategoryApplicability.BOTH);
        assertThatThrownBy(() -> save(ownerA, "Alimentacao", CategoryApplicability.BOTH))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(categories.findByIdAndOwnerId(saved.getId(), ownerA).orElseThrow().getName())
            .isEqualTo("Alimentação");
    }

    @Test
    void shouldAllowSameNameForDifferentOwners() {
        Category a = save(ownerA, "Outros", CategoryApplicability.BOTH);
        Category b = save(ownerB, "Outros", CategoryApplicability.BOTH);
        assertThat(a.getId()).isNotEqualTo(b.getId());
    }

    @Test
    void shouldAllowMultipleInactiveNamesAndAnotherActiveCategory() {
        for (int i = 0; i < 3; i++) {
            Category previous = save(ownerA, "Outros", CategoryApplicability.BOTH);
            previous.deactivate();
            categories.saveAndFlush(previous);
        }
        Category current = save(ownerA, "Outros", CategoryApplicability.INCOME);
        assertThat(categories.findAllByOwnerIdOrderByNameAsc(ownerA)).hasSize(4);
        assertThat(categories.findAllByOwnerIdAndActiveTrueOrderByNameAsc(ownerA))
            .extracting(Category::getId).containsExactly(current.getId());
    }

    @Test
    void shouldRejectConflictingReactivation() {
        Category previous = save(ownerA, "Outros", CategoryApplicability.BOTH);
        previous.deactivate();
        previous = categories.saveAndFlush(previous);
        save(ownerA, "Outros", CategoryApplicability.EXPENSE);
        previous.activate();
        Category reactivated = previous;
        assertThatThrownBy(() -> categories.saveAndFlush(reactivated))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectConflictingRename() {
        save(ownerA, "Outros", CategoryApplicability.BOTH);
        Category renamed = save(ownerA, "Diversos", CategoryApplicability.INCOME);
        renamed.rename(" OUTROS ");
        assertThatThrownBy(() -> categories.saveAndFlush(renamed))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectNonexistentOwner() {
        assertThatThrownBy(() -> save(UUID.randomUUID().toString(), "Outros", CategoryApplicability.BOTH))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPreventDeletingOwnerWithCategoryHistory() {
        Category archived = save(ownerA, "Histórico", CategoryApplicability.BOTH);
        archived.deactivate();
        categories.saveAndFlush(archived);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            users.deleteById(ownerA);
            users.flush();
        })).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectInvalidPersistedApplicability() {
        Category saved = save(ownerA, "Outros", CategoryApplicability.BOTH);
        assertThatThrownBy(() -> jdbc.update("UPDATE categories SET applicability = ? WHERE id = ?", "INVALID", saved.getId()))
            .isInstanceOfSatisfying(UncategorizedSQLException.class, error -> {
                assertThat(error.getSQLException().getErrorCode()).isEqualTo(3819);
                assertThat(error.getSQLException().getMessage()).contains("ck_categories_applicability");
            });
        assertThat(categories.findByIdAndOwnerId(saved.getId(), ownerA).orElseThrow().getApplicability())
            .isEqualTo(CategoryApplicability.BOTH);
    }
}
