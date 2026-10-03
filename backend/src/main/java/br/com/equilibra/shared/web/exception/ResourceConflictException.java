package br.com.equilibra.shared.web.exception;

/**
 * Exceção reutilizável para conflitos de estado ou unicidade.
 *
 * <p>A tradução para HTTP deve permanecer no {@link GlobalExceptionHandler}.</p>
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
