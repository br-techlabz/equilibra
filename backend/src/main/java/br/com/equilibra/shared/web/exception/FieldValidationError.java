package br.com.equilibra.shared.web.exception;

/**
 * Representa um erro de validação associado a um campo de request.
 */
public record FieldValidationError(String field, String message) {
}
