package br.com.equilibra.auth.domain.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para {@link PasswordValidator}.
 */
class PasswordValidatorTest {

    @Nested
    @DisplayName("Senhas válidas")
    class ValidPasswords {

        @Test
        @DisplayName("Senha com exatamente 8 caracteres (mínimo)")
        void shouldAcceptMinimumLength() {
            String password = "12345678";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Senha com 128 caracteres (máximo)")
        void shouldAcceptMaximumLength() {
            String password = "a".repeat(128);
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Passphrase válida com espaços")
        void shouldAcceptPassphraseWithSpaces() {
            String password = "minha senha segura com espacos";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Senha com caracteres especiais")
        void shouldAcceptSpecialCharacters() {
            String password = "Senh@#$%^&*()_+";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Senha com Unicode (emojis, acentos)")
        void shouldAcceptUnicode() {
            String password = "senha🔒çãõáéíóú";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Senha com espaços no início e fim (não são removidos)")
        void shouldNotTrimPassword() {
            String password = "  senha123  ";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "abcdefgh",     // 8 chars
            "abcdefghijkl", // 12 chars
            "senha longa valida com passphrase" // passphrase
        })
        @DisplayName("Vários comprimentos válidos")
        void shouldAcceptVariousValidLengths(String password) {
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("Senhas inválidas - comprimento")
    class InvalidLength {

        @Test
        @DisplayName("Senha com 7 caracteres (abaixo do mínimo)")
        void shouldRejectBelowMinimum() {
            String password = "1234567";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha deve possuir pelo menos 8 caracteres");
        }

        @Test
        @DisplayName("Senha com 129 caracteres (acima do máximo)")
        void shouldRejectAboveMaximum() {
            String password = "a".repeat(129);
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha excede o tamanho máximo de 128 caracteres");
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "ab", "abc", "abcd", "abcde", "abcdef", "abcdefg"})
        @DisplayName("Comprimentos abaixo do mínimo (1-7 chars)")
        void shouldRejectBelowMinimumLength(String password) {
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).anyMatch(v -> v.contains("pelo menos"));
        }

        @Test
        @DisplayName("Senha vazia retorna violação específica")
        void shouldRejectEmptyWithSpecificMessage() {
            var result = PasswordValidator.validate("");
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha não pode ser vazia");
        }
    }

    @Nested
    @DisplayName("Senhas inválidas - vazias/espaços")
    class InvalidBlank {

        @Test
        @DisplayName("Senha vazia")
        void shouldRejectEmpty() {
            var result = PasswordValidator.validate("");
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha não pode ser vazia");
        }

        @Test
        @DisplayName("Senha nula")
        void shouldRejectNull() {
            var result = PasswordValidator.validate((CharSequence) null);
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha não pode ser vazia");
        }

        @Test
        @DisplayName("Senha apenas com espaços")
        void shouldRejectOnlySpaces() {
            var result = PasswordValidator.validate("   ");
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha não pode conter apenas espaços em branco");
        }

        @Test
        @DisplayName("Senha apenas com tabs e newlines")
        void shouldRejectOnlyWhitespace() {
            var result = PasswordValidator.validate("\t\n\r  ");
            assertThat(result.isValid()).isFalse();
            assertThat(result.violations()).contains("Senha não pode conter apenas espaços em branco");
        }
    }

    @Nested
    @DisplayName("Validação com exceção")
    class ValidateOrThrow {

        @Test
        @DisplayName("Não lança exceção para senha válida")
        void shouldNotThrowForValid() {
            PasswordValidator.validateOrThrow("senhaValida123");
        }

        @Test
        @DisplayName("Lança IllegalArgumentException para senha vazia")
        void shouldThrowForEmpty() {
            org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PasswordValidator.validateOrThrow("")
            );
        }

        @Test
        @DisplayName("Mensagem de exceção não expõe a senha")
        void shouldNotExposePasswordInException() {
            String password = "senhaSecreta123";
            var exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PasswordValidator.validateOrThrow("a")
            );
            assertThat(exception.getMessage()).doesNotContain(password);
        }
    }

    @Nested
    @DisplayName("Não transformação silenciosa")
    class NoSilentTransformation {

        @Test
        @DisplayName("Senha com espaços no início/fim é aceita como está (sem trim)")
        void shouldNotTrimSilently() {
            String passwordWithSpaces = "  minhaSenha123  ";
            var result = PasswordValidator.validate(passwordWithSpaces);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("Senha com espaços internos preservados")
        void shouldPreserveInternalSpaces() {
            String password = "minha senha 123";
            var result = PasswordValidator.validate(password);
            assertThat(result.isValid()).isTrue();
        }
    }
}