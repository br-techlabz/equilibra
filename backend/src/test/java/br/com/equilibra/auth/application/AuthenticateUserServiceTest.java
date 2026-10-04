package br.com.equilibra.auth.application;

import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    @Test
    void shouldAuthenticateActiveUserWithCorrectPassword() {
        User user = new User("bill@example.com", "hash-seguro");
        when(userRepository.findByEmail("bill@example.com")).thenReturn(Optional.of(user));
        when(passwordService.matches("senhaValida123", "hash-seguro")).thenReturn(true);

        AuthenticatedUser authenticatedUser = authenticateUserService.authenticate(
            new AuthenticateUserCommand(" Bill@Example.com ", "senhaValida123")
        );

        verify(userRepository).findByEmail("bill@example.com");
        verify(passwordService).matches("senhaValida123", "hash-seguro");
        assertThat(authenticatedUser.userId()).isEqualTo(user.getId());
        assertThat(authenticatedUser.email()).isEqualTo("bill@example.com");
        assertThat(authenticatedUser).hasNoNullFieldsOrProperties();
    }

    @Test
    void shouldRejectIncorrectPassword() {
        User user = new User("bill@example.com", "hash-seguro");
        when(userRepository.findByEmail("bill@example.com")).thenReturn(Optional.of(user));
        when(passwordService.matches("senhaErrada123", "hash-seguro")).thenReturn(false);

        assertThatThrownBy(() -> authenticateUserService.authenticate(
                new AuthenticateUserCommand("bill@example.com", "senhaErrada123")
            ))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password.");
    }

    @Test
    void shouldRejectNonExistentUserUsingDummyHashVerification() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        when(passwordService.matches("senhaValida123", "$2a$10$rGbqgrmwYRiZTHFidimEUOiujOMDVHOXCWbWznXeez4OWfKyt8G0u"))
            .thenReturn(false);

        assertThatThrownBy(() -> authenticateUserService.authenticate(
                new AuthenticateUserCommand("missing@example.com", "senhaValida123")
            ))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password.");

        verify(passwordService).matches("senhaValida123", "$2a$10$rGbqgrmwYRiZTHFidimEUOiujOMDVHOXCWbWznXeez4OWfKyt8G0u");
    }

    @Test
    void shouldRejectInactiveUserUsingDummyHashVerification() {
        User user = new User("inactive@example.com", "hash-real");
        user.setActive(false);
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));
        when(passwordService.matches("senhaValida123", "$2a$10$rGbqgrmwYRiZTHFidimEUOiujOMDVHOXCWbWznXeez4OWfKyt8G0u"))
            .thenReturn(false);

        assertThatThrownBy(() -> authenticateUserService.authenticate(
                new AuthenticateUserCommand("inactive@example.com", "senhaValida123")
            ))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Invalid email or password.");

        verify(passwordService).matches("senhaValida123", "$2a$10$rGbqgrmwYRiZTHFidimEUOiujOMDVHOXCWbWznXeez4OWfKyt8G0u");
    }
}
