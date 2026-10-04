package br.com.equilibra.auth.domain.validator;

import br.com.equilibra.auth.domain.policy.PasswordPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Validador de senha reutilizável.
 * <p>
 * Aplica a {@link PasswordPolicy} e retorna lista de violações claras
 * sem expor a senha em mensagens de erro.
 * </p>
 * <p>
 * Não lança exceção; retorna lista vazia quando válida.
 * </p>
 */
public final class PasswordValidator {

    private PasswordValidator() {
        // Classe utilitária, não instanciável
    }

    /**
     * Resultado da validação de senha.
     */
    public record ValidationResult(boolean isValid, List<String> violations) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(List<String> violations) {
            return new ValidationResult(false, List.copyOf(violations));
        }
    }

    /**
     * Valida a senha contra a política definida.
     *
     * @param rawPassword senha em texto puro (não será modificada)
     * @return resultado com violações, se houver
     */
    public static ValidationResult validate(CharSequence rawPassword) {
        List<String> violations = new ArrayList<>();

        if (!PasswordPolicy.isNotBlank(rawPassword)) {
            violations.add("Senha não pode ser vazia");
            return ValidationResult.failure(violations);
        }

        if (!PasswordPolicy.isNotOnlyWhitespace(rawPassword)) {
            violations.add("Senha não pode conter apenas espaços em branco");
            return ValidationResult.failure(violations);
        }

        if (!PasswordPolicy.meetsMinLength(rawPassword)) {
            violations.add("Senha deve possuir pelo menos " + PasswordPolicy.MIN_LENGTH + " caracteres");
        }

        if (!PasswordPolicy.meetsMaxLength(rawPassword)) {
            violations.add("Senha excede o tamanho máximo de " + PasswordPolicy.MAX_LENGTH + " caracteres");
        }

        return violations.isEmpty() ? ValidationResult.success() : ValidationResult.failure(violations);
    }

    /**
     * Valida e lança exceção se inválida (para uso em serviços de aplicação).
     *
     * @param rawPassword senha em texto puro
     * @throws IllegalArgumentException se a senha violar a política
     */
    public static void validateOrThrow(CharSequence rawPassword) {
        ValidationResult result = validate(rawPassword);
        if (!result.isValid()) {
            throw new IllegalArgumentException(String.join("; ", result.violations()));
        }
    }
}