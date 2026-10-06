package br.com.equilibra.tag.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagTest {
    @Test
    void shouldNormalizeNameAndPreservePresentation() {
        Tag tag = new Tag(UUID.randomUUID().toString(), "  Viagem São Paulo  ");
        assertThat(tag.getName()).isEqualTo("Viagem São Paulo");
        assertThat(tag.getNormalizedName()).isEqualTo("viagem são paulo");
        assertThat(tag.isActive()).isTrue();
    }

    @Test
    void shouldRejectBlankAndLongNames() {
        String owner = UUID.randomUUID().toString();
        assertThatThrownBy(() -> new Tag(owner, "   ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tag(owner, "a".repeat(101))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldChangeLifecycleAndName() {
        Tag tag = new Tag(UUID.randomUUID().toString(), "Viagem");
        tag.deactivate();
        assertThat(tag.isActive()).isFalse();
        tag.rename("Trabalho");
        tag.activate();
        assertThat(tag.getName()).isEqualTo("Trabalho");
        assertThat(tag.getNormalizedName()).isEqualTo("trabalho");
        assertThat(tag.isActive()).isTrue();
    }
}
