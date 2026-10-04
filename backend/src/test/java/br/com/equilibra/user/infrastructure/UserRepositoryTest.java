package br.com.equilibra.user.infrastructure;

import br.com.equilibra.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Testes de integração para {@link UserRepository}.
 * <p>
 * Utiliza Testcontainers com MySQL para garantir comportamento real do banco.
 * Profile 'test' configura Testcontainers automaticamente.
 * </p>
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndFindUserById() {
        // Given
        User user = new User("test@example.com", "hashed-password-123");

        // When
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getPasswordHash()).isEqualTo("hashed-password-123");
        assertThat(found.get().isActive()).isTrue();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindUserByEmail() {
        // Given
        String email = "findme@example.com";
        User user = new User(email, "hashed-password-456");
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("findme@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void shouldFindUserByEmailCaseInsensitive() {
        // Given
        String email = "CaseSensitive@Example.COM";
        User user = new User(email, "hashed-password-789");
        userRepository.save(user);

        // When - busca com casing diferente
        Optional<User> foundLower = userRepository.findByEmail("casesensitive@example.com");
        Optional<User> foundUpper = userRepository.findByEmail("CASESENSITIVE@EXAMPLE.COM");
        Optional<User> foundMixed = userRepository.findByEmail("CaseSensitive@EXAMPLE.com");

        // Then - todos devem encontrar o mesmo usuário (normalização lowercase)
        assertAll(
            () -> assertThat(foundLower).isPresent(),
            () -> assertThat(foundUpper).isPresent(),
            () -> assertThat(foundMixed).isPresent(),
            () -> assertThat(foundLower.get().getId()).isEqualTo(foundUpper.get().getId()),
            () -> assertThat(foundLower.get().getId()).isEqualTo(foundMixed.get().getId())
        );
    }

    @Test
    void shouldDetectEmailUniquenessViolation() {
        // Given
        String email = "unique@example.com";
        User user1 = new User(email, "hash-1");
        userRepository.save(user1);
        User user2 = new User(email, "hash-2"); // mesmo email

        // When / Then
        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void shouldReturnTrueForExistsByEmail() {
        // Given
        String email = "exists@example.com";
        User user = new User(email, "hashed-password");
        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("exists@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseForNonExistentEmail() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldSetActiveTrueByDefault() {
        // Given
        User user = new User("defaultactive@example.com", "hash");

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnPersist() {
        // Given
        User user = new User("timestamps@example.com", "hash");

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    void shouldUpdateUpdatedAtOnUpdate() {
        // Given
        User user = new User("update@example.com", "hash");
        User saved = userRepository.save(user);
        Instant originalUpdatedAt = saved.getUpdatedAt();

        // When - aguarda um pouco para garantir timestamp diferente
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        saved.setActive(false);
        User updated = userRepository.save(saved);

        // Then
        assertThat(updated.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updated.getCreatedAt()).isEqualTo(saved.getCreatedAt()); // createdAt não muda
    }

    @Test
    void shouldNormalizeEmailOnPersist() {
        // Given
        String emailWithSpaces = "  Spaced@Example.COM  ";
        User user = new User(emailWithSpaces, "hash");

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getEmail()).isEqualTo("spaced@example.com");
    }

    @Test
    void shouldNormalizeEmailOnUpdate() {
        // Given
        User user = new User("original@example.com", "hash");
        User saved = userRepository.save(user);

        // When - atualiza com email com espaços e maiúsculas
        saved.setEmail("  UPDATED@EXAMPLE.COM  ");
        User updated = userRepository.save(saved);

        // Then
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }
}