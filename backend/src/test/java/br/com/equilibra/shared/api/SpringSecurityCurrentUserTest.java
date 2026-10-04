package br.com.equilibra.shared.api;

import br.com.equilibra.auth.infrastructure.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpringSecurityCurrentUserTest {

    @Test
    void shouldReturnUserIdWhenAuthenticatedWithValidPrincipal() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, AuthorityUtils.NO_AUTHORITIES
        );

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(authentication);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            SpringSecurityCurrentUser currentUser = new SpringSecurityCurrentUser();
            UUID result = currentUser.id();

            assertThat(result).isEqualTo(UUID.fromString(userId));
        }
    }

    @Test
    void shouldThrowWhenNoAuthenticationInContext() {
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(null);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            SpringSecurityCurrentUser currentUser = new SpringSecurityCurrentUser();

            assertThatThrownBy(currentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authentication found in SecurityContext");
        }
    }

    @Test
    void shouldThrowWhenAuthenticationIsAnonymous() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(anonymous);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            SpringSecurityCurrentUser currentUser = new SpringSecurityCurrentUser();

            assertThatThrownBy(currentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authentication is anonymous");
        }
    }

    @Test
    void shouldThrowWhenPrincipalIsNotAuthenticatedPrincipal() {
        String unexpectedPrincipal = "unexpected-string";

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            unexpectedPrincipal, null, AuthorityUtils.NO_AUTHORITIES
        );

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(authentication);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            SpringSecurityCurrentUser currentUser = new SpringSecurityCurrentUser();

            assertThatThrownBy(currentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unexpected principal type");
        }
    }

    @Test
    void shouldThrowWhenUserIdIsBlank() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal("");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, AuthorityUtils.NO_AUTHORITIES
        );

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(authentication);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            SpringSecurityCurrentUser currentUser = new SpringSecurityCurrentUser();

            assertThatThrownBy(currentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AuthenticatedPrincipal.userId is blank");
        }
    }

    @Test
    void shouldThrowWhenUserIdIsNotValidUuid() {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal("not-a-uuid");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, AuthorityUtils.NO_AUTHORITIES
        );

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(authentication);
            mocked.when(SecurityContextHolder::getContext).thenReturn(context);

            SpringSecurityCurrentUser currentUser = new SpringSecurityCurrentUser();

            assertThatThrownBy(currentUser::id)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a valid UUID");
        }
    }
}