package br.com.equilibra.user.application;

import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.user.api.CurrentUserResponse;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso para obter informações do usuário autenticado.
 */
@Service
public class GetCurrentUserService {

    private final CurrentUser currentUser;
    private final UserRepository userRepository;

    public GetCurrentUserService(CurrentUser currentUser, UserRepository userRepository) {
        this.currentUser = currentUser;
        this.userRepository = userRepository;
    }

    /**
     * Retorna o usuário autenticado.
     *
     * @return response com id, email e createdAt do usuário
     * @throws IllegalStateException se não houver autenticação válida
     * @throws IllegalArgumentException se o ID autenticado não for UUID válido
     * @throws ResourceNotFoundException se o usuário não existir mais no banco
     */
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        UUID userId = currentUser.id();
        User user = userRepository.findById(userId.toString())
            .filter(User::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}