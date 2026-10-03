package br.com.equilibra.shared.web.exception;

/**
 * Exceção reutilizável para recursos não encontrados.
 *
 * <p>A tradução para HTTP deve permanecer no {@link GlobalExceptionHandler}.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
