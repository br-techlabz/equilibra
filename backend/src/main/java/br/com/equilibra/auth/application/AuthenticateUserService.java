package br.com.equilibra.auth.application;

import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de autenticação por email e senha.
 */
@Service
public class AuthenticateUserService {

    /**
     * Hash BCrypt fictício usado para executar verificação equivalente quando o usuário não existe
     * ou não está autenticável, reduzindo diferenças triviais de fluxo sem usar sleeps artificiais.
     */
    private static final String DUMMY_PASSWORD_HASH = "$2a$10$rGbqgrmwYRiZTHFidimEUOiujOMDVHOXCWbWznXeez4OWfKyt8G0u";

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthenticateUserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Autentica credenciais estruturalmente válidas sem criar sessão ou token.
     *
     * @param command credenciais informadas
     * @return identidade interna autenticada
     * @throws InvalidCredentialsException quando email/senha não autenticam
     */
    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(AuthenticateUserCommand command) {
        String normalizedEmail = User.normalizeEmail(command.email());
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        boolean userCanAuthenticate = user != null && user.isActive();
        String passwordHash = userCanAuthenticate ? user.getPasswordHash() : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = passwordService.matches(command.password(), passwordHash);

        if (!userCanAuthenticate || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        return new AuthenticatedUser(user.getId(), user.getEmail());
    }
}
