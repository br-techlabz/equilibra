package br.com.equilibra.auth.application;

import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.user.domain.User;
import br.com.equilibra.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    void shouldNormalizeEmailHashPasswordAndPersistUser() {
        when(userRepository.existsByEmail("bill@example.com")).thenReturn(false);
        when(passwordService.encode("senhaValida123")).thenReturn("hash-seguro");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterUserResult result = registerUserService.register(
            new RegisterUserCommand(" Bill@Example.com ", "senhaValida123")
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).existsByEmail("bill@example.com");
        verify(passwordService).encode("senhaValida123");
        verify(userRepository).saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("bill@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hash-seguro");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("senhaValida123");
        assertThat(result.id()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo("bill@example.com");
    }

    @Test
    void shouldRejectExistingEmail() {
        when(userRepository.existsByEmail("duplicado@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registerUserService.register(
                new RegisterUserCommand(" Duplicado@Example.com ", "senhaValida123")
            ))
            .isInstanceOf(ResourceConflictException.class)
            .hasMessage("An account with this email already exists.");

        verify(passwordService, never()).encode(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldNotPersistWhenPasswordIsInvalid() {
        when(userRepository.existsByEmail("valid@example.com")).thenReturn(false);
        when(passwordService.encode("curta")).thenThrow(new IllegalArgumentException("Senha deve possuir pelo menos 8 caracteres"));

        assertThatThrownBy(() -> registerUserService.register(
                new RegisterUserCommand("valid@example.com", "curta")
            ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("pelo menos");

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldConvertEmailUniqueViolationToConflict() {
        when(userRepository.existsByEmail("race@example.com")).thenReturn(false);
        when(passwordService.encode("senhaValida123")).thenReturn("hash-seguro");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(
            new DataIntegrityViolationException("Duplicate entry 'race@example.com' for key 'uk_users_email'")
        );

        assertThatThrownBy(() -> registerUserService.register(
                new RegisterUserCommand("race@example.com", "senhaValida123")
            ))
            .isInstanceOf(ResourceConflictException.class)
            .hasMessage("An account with this email already exists.");
    }

    @Test
    void shouldNotConvertUnrelatedDataIntegrityViolationToConflict() {
        when(userRepository.existsByEmail("valid@example.com")).thenReturn(false);
        when(passwordService.encode("senhaValida123")).thenReturn("hash-seguro");
        DataIntegrityViolationException databaseError = new DataIntegrityViolationException("Data too long for column 'other_column'");
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(databaseError);

        assertThatThrownBy(() -> registerUserService.register(
                new RegisterUserCommand("valid@example.com", "senhaValida123")
            ))
            .isSameAs(databaseError);
    }
}
