package br.com.equilibra.auth.application;

import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de cadastro de usuário.
 */
@Service
public class RegisterUserService {

    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "An account with this email already exists.";

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public RegisterUserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Registra uma nova conta de usuário sem autenticar automaticamente.
     *
     * @param command dados validados de cadastro
     * @return resultado seguro do usuário criado
     */
    @Transactional
    public RegisterUserResult register(RegisterUserCommand command) {
        String normalizedEmail = User.normalizeEmail(command.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw emailAlreadyExists();
        }

        String passwordHash = passwordService.encode(command.password());
        User user = new User(normalizedEmail, passwordHash);

        try {
            User saved = userRepository.saveAndFlush(user);
            return new RegisterUserResult(saved.getId(), saved.getEmail(), saved.getCreatedAt());
        } catch (DataIntegrityViolationException ex) {
            if (isEmailUniquenessViolation(ex)) {
                throw emailAlreadyExists();
            }
            throw ex;
        }
    }

    private ResourceConflictException emailAlreadyExists() {
        return new ResourceConflictException(EMAIL_ALREADY_EXISTS_MESSAGE);
    }

    private boolean isEmailUniquenessViolation(DataIntegrityViolationException ex) {
        String message = collectExceptionMessages(ex).toLowerCase();
        boolean duplicateEntry = message.contains("duplicate entry") || message.contains("constraint") || message.contains("unique");
        boolean emailConstraint = message.contains("uk_users_email") || message.contains("users.email") || message.contains("email");
        return duplicateEntry && emailConstraint;
    }

    private String collectExceptionMessages(Throwable throwable) {
        StringBuilder messages = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                messages.append(message).append(' ');
            }
            current = current.getCause();
        }
        return messages.toString();
    }
}
