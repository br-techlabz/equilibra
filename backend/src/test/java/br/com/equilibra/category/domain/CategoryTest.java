package br.com.equilibra.category.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    private final String ownerId = UUID.randomUUID().toString();

    @ParameterizedTest
    @EnumSource(CategoryApplicability.class)
    void shouldCreateValidCategory(CategoryApplicability applicability) {
        Category category = new Category(ownerId, " Alimentação ", applicability);

        assertThat(UUID.fromString(category.getId()).toString()).isEqualTo(category.getId());
        assertThat(category.getOwnerId()).isEqualTo(ownerId);
        assertThat(category.getName()).isEqualTo("Alimentação");
        assertThat(category.getNormalizedName()).isEqualTo("alimentação");
        assertThat(category.getApplicability()).isEqualTo(applicability);
        assertThat(category.isActive()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", " "})
    void shouldRejectBlankNames(String name) {
        assertThatThrownBy(() -> new Category(ownerId, name, CategoryApplicability.EXPENSE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldRejectMissingOwner(String owner) {
        assertThatThrownBy(() -> new Category(owner, "Outros", CategoryApplicability.BOTH))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldEnforceNameLength() {
        assertThat(new Category(ownerId, "a".repeat(100), CategoryApplicability.BOTH).getName())
            .hasSize(100);
        assertThatThrownBy(() -> new Category(ownerId, "a".repeat(101), CategoryApplicability.BOTH))
            .isInstanceOf(IllegalArgumentException.class);
        // Lowercasing U+0130 expands to two code points under Locale.ROOT.
        assertThatThrownBy(() -> new Category(ownerId, "İ".repeat(100), CategoryApplicability.BOTH))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRequireApplicability() {
        assertThatThrownBy(() -> new Category(ownerId, "Outros", null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNormalizeWithoutRemovingAccents() {
        assertThat(Category.normalizeName(" ALIMENTAÇÃO ")).isEqualTo("alimentação");
        assertThat(Category.normalizeName(" Alimentação ")).isEqualTo("alimentação");
        assertThat(Category.normalizeName("Alimentacao")).isEqualTo("alimentacao");
    }

    @Test
    void shouldNormalizeIndependentlyOfDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(Category.normalizeName(" INVESTIMENTOS ")).isEqualTo("investimentos");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void shouldRenameAndPreserveIdentity() {
        Category category = new Category(ownerId, "Outros", CategoryApplicability.BOTH);
        String id = category.getId();
        category.rename(" EDUCAÇÃO ");
        assertThat(category.getName()).isEqualTo("EDUCAÇÃO");
        assertThat(category.getNormalizedName()).isEqualTo("educação");
        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getOwnerId()).isEqualTo(ownerId);

        assertThatThrownBy(() -> category.rename(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThat(category.getName()).isEqualTo("EDUCAÇÃO");
        assertThat(category.getNormalizedName()).isEqualTo("educação");
    }

    @Test
    void shouldChangeApplicabilityWithoutAllowingNull() {
        Category category = new Category(ownerId, "Outros", CategoryApplicability.EXPENSE);
        for (CategoryApplicability value : CategoryApplicability.values()) {
            category.changeApplicability(value);
            assertThat(category.getApplicability()).isEqualTo(value);
        }
        assertThatThrownBy(() -> category.changeApplicability(null)).isInstanceOf(NullPointerException.class);
        assertThat(category.getApplicability()).isEqualTo(CategoryApplicability.BOTH);
    }

    @Test
    void shouldActivateAndDeactivateIdempotently() {
        Category category = new Category(ownerId, "Outros", CategoryApplicability.BOTH);
        category.deactivate();
        category.deactivate();
        assertThat(category.isActive()).isFalse();
        category.activate();
        category.activate();
        assertThat(category.isActive()).isTrue();
    }
}
