package br.com.equilibra.shared.web.exception;

import br.com.equilibra.auth.application.InvalidCredentialsException;
import br.com.equilibra.shared.web.filter.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Traduz exceções da aplicação para respostas HTTP padronizadas com Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            "One or more fields are invalid.",
            request,
            "validation-failed"
        );

        List<FieldValidationError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(error -> new FieldValidationError(error.getField(), resolveValidationMessage(error)))
            .toList();

        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.BAD_REQUEST,
            "Malformed request",
            "The request body is invalid or malformed.",
            request,
            "malformed-request"
        );
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMediaTypeNotSupportedException.class,
        HttpRequestMethodNotSupportedException.class
    })
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.BAD_REQUEST,
            "Bad request",
            "The request is invalid.",
            request,
            "bad-request"
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.NOT_FOUND,
            "Resource not found",
            ex.getMessage(),
            request,
            "resource-not-found"
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ProblemDetail handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.NOT_FOUND,
            "Resource not found",
            "The requested resource was not found.",
            request,
            "resource-not-found"
        );
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ProblemDetail handleConflict(ResourceConflictException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.CONFLICT,
            "Resource conflict",
            ex.getMessage(),
            request,
            "resource-conflict"
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.UNAUTHORIZED,
            "Authentication failed",
            InvalidCredentialsException.MESSAGE,
            request,
            "authentication-failed"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "Authentication is required to access this resource.",
            request,
            "unauthorized"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return createProblem(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "You do not have permission to access this resource.",
            request,
            "forbidden"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error while processing request", ex);

        return createProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected error occurred.",
            request,
            "internal-server-error"
        );
    }

    private ProblemDetail createProblem(
        HttpStatus status,
        String title,
        String detail,
        HttpServletRequest request,
        String type
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://equilibra.com.br/problems/" + type));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("requestId", resolveRequestId(request));
        return problem;
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY);
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }

        Object attribute = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return attribute instanceof String value ? value : null;
    }

    private String resolveValidationMessage(FieldError error) {
        String defaultMessage = error.getDefaultMessage();
        if (defaultMessage == null || defaultMessage.isBlank()) {
            return "Invalid value.";
        }

        return defaultMessage;
    }
}
