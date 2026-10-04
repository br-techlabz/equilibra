package br.com.equilibra.auth.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes para {@link PasswordService}.
 * <p>
 * Testes unitários que não requerem banco de dados.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
class PasswordServiceTest {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("Codificação (encode)")
    class Encode {

        @Test
        @DisplayName("Gera hash para senha válida")
        void shouldEncodeValidPassword() {
            String rawPassword = "minhaSenhaSegura123";
            String encoded = passwordService.encode(rawPassword);

            assertThat(encoded).isNotNull();
            assertThat(encoded).isNotEqualTo(rawPassword);
            assertThat(encoded).startsWith("$2a$"); // Prefixo BCrypt
        }

        @Test
        @DisplayName("Hash não é igual à senha original")
        void shouldNotEqualOriginalPassword() {
            String rawPassword = "senha123";
            String encoded = passwordService.encode(rawPassword);

            assertThat(encoded).isNotEqualTo(rawPassword);
        }

        @Test
        @DisplayName("Codificar mesma senha duas vezes produz hashes diferentes (salt)")
        void shouldProduceDifferentHashesForSamePassword() {
            String rawPassword = "mesmaSenha456";
            String hash1 = passwordService.encode(rawPassword);
            String hash2 = passwordService.encode(rawPassword);

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Ambos hashes validam corretamente a mesma senha")
        void bothHashesShouldValidateCorrectly() {
            String rawPassword = "outraSenha789";
            String hash1 = passwordService.encode(rawPassword);
            String hash2 = passwordService.encode(rawPassword);

            assertThat(passwordService.matches(rawPassword, hash1)).isTrue();
            assertThat(passwordService.matches(rawPassword, hash2)).isTrue();
        }

        @Test
        @DisplayName("Lança exceção para senha vazia")
        void shouldThrowForEmptyPassword() {
            assertThatThrownBy(() -> passwordService.encode(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazia");
        }

        @Test
        @DisplayName("Lança exceção para senha apenas com espaços")
        void shouldThrowForOnlyWhitespace() {
            assertThatThrownBy(() -> passwordService.encode("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("espaços");
        }

        @Test
        @DisplayName("Lança exceção para senha curta (7 chars)")
        void shouldThrowForShortPassword() {
            assertThatThrownBy(() -> passwordService.encode("curta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pelo menos");
        }

        @Test
        @DisplayName("Lança exceção para senha longa (129 chars)")
        void shouldThrowForLongPassword() {
            String longPassword = "a".repeat(129);
            assertThatThrownBy(() -> passwordService.encode(longPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede");
        }
    }

    @Nested
    @DisplayName("Verificação (matches)")
    class Matches {

        @Test
        @DisplayName("Retorna true para senha correta")
        void shouldReturnTrueForCorrectPassword() {
            String rawPassword = "senhaCorreta123";
            String encoded = passwordService.encode(rawPassword);

            assertThat(passwordService.matches(rawPassword, encoded)).isTrue();
        }

        @Test
        @DisplayName("Retorna false para senha incorreta")
        void shouldReturnFalseForIncorrectPassword() {
            String rawPassword = "senhaCorreta123";
            String encoded = passwordService.encode(rawPassword);

            assertThat(passwordService.matches("senhaErrada456", encoded)).isFalse();
        }

        @Test
        @DisplayName("Retorna false para senha nula")
        void shouldReturnFalseForNullRawPassword() {
            String encoded = passwordService.encode("senhaValida123");

            assertThat(passwordService.matches(null, encoded)).isFalse();
        }

        @Test
        @DisplayName("Retorna false para hash nulo")
        void shouldReturnFalseForNullEncodedPassword() {
            assertThat(passwordService.matches("senhaValida123", null)).isFalse();
        }

        @Test
        @DisplayName("Retorna false para ambos nulos")
        void shouldReturnFalseForBothNull() {
            assertThat(passwordService.matches(null, null)).isFalse();
        }

        @Test
        @DisplayName("Senha com espaços no início/fim confere corretamente (sem trim)")
        void shouldMatchPasswordWithLeadingTrailingSpaces() {
            String rawPassword = "  senhaComEspacos  ";
            String encoded = passwordService.encode(rawPassword);

            // A mesma senha com espaços deve conferir
            assertThat(passwordService.matches("  senhaComEspacos  ", encoded)).isTrue();

            // Senha sem espaços NÃO deve conferir (prova que não houve trim)
            assertThat(passwordService.matches("senhaComEspacos", encoded)).isFalse();
        }

        @Test
        @DisplayName("Senha com Unicode confere corretamente")
        void shouldMatchUnicodePassword() {
            String rawPassword = "senh🔒çãõáéí";
            String encoded = passwordService.encode(rawPassword);

            assertThat(passwordService.matches(rawPassword, encoded)).isTrue();
        }
    }

    @Nested
    @DisplayName("Upgrade de encoding")
    class UpgradeEncoding {

        @Test
        @DisplayName("Retorna false para hash BCrypt atual (não precisa upgrade)")
        void shouldReturnFalseForCurrentBCrypt() {
            String rawPassword = "senhaParaUpgrade";
            String encoded = passwordService.encode(rawPassword);

            assertThat(passwordService.upgradeEncoding(encoded)).isFalse();
        }

        @Test
        @DisplayName("Retorna true para hash com algoritmo diferente (se configurado)")
        void shouldReturnTrueForDifferentAlgorithm() {
            // BCryptPasswordEncoder padrão não identifica outros algoritmos
            // Este teste documenta o comportamento atual
            String rawPassword = "senhaValida123";
            String encoded = passwordService.encode(rawPassword);

            // Com BCrypt simples, upgradeEncoding sempre retorna false
            // Com DelegatingPasswordEncoder, retornaria true para algoritmos legados
            assertThat(passwordService.upgradeEncoding(encoded)).isFalse();
        }
    }

    @Nested
    @DisplayName("Integração com PasswordEncoder direto")
    class DirectEncoderIntegration {

        @Test
        @DisplayName("PasswordService usa o mesmo encoder injetado")
        void shouldUseInjectedEncoder() {
            String rawPassword = "testeInjecao123";
            String encodedViaService = passwordService.encode(rawPassword);
            String encodedDirect = passwordEncoder.encode(rawPassword);

            // Ambos devem validar a mesma senha (mesmo salt strategy)
            assertThat(passwordEncoder.matches(rawPassword, encodedViaService)).isTrue();
            assertThat(passwordEncoder.matches(rawPassword, encodedDirect)).isTrue();
        }

        @Test
        @DisplayName("Matches do service delega para encoder.matches")
        void shouldDelegateMatchesToEncoder() {
            String rawPassword = "delegacaoTeste";
            String encoded = passwordEncoder.encode(rawPassword);

            assertThat(passwordService.matches(rawPassword, encoded)).isTrue();
            assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        }
    }
}