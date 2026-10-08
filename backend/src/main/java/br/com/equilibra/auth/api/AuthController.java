package br.com.equilibra.auth.api;

import br.com.equilibra.auth.application.AuthenticateUserCommand;
import br.com.equilibra.auth.application.AuthenticateUserService;
import br.com.equilibra.auth.application.AuthenticatedUser;
import br.com.equilibra.auth.application.JwtTokenService;
import br.com.equilibra.auth.application.LoginTokenResponse;
import br.com.equilibra.auth.application.RegisterUserCommand;
import br.com.equilibra.auth.application.RegisterUserResult;
import br.com.equilibra.auth.application.RegisterUserService;
import br.com.equilibra.auth.application.RefreshSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de autenticação e identidade.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Fluxos públicos de identidade e autenticação")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final AuthenticateUserService authenticateUserService;
    private final JwtTokenService jwtTokenService;
    private final RefreshSessionService refreshSessions;
    private final boolean secureRefreshCookie;

    public AuthController(
        RegisterUserService registerUserService,
        AuthenticateUserService authenticateUserService,
        JwtTokenService jwtTokenService,
        RefreshSessionService refreshSessions,
        @Value("${equilibra.security.refresh-cookie-secure:false}") boolean secureRefreshCookie
    ) {
        this.registerUserService = registerUserService;
        this.authenticateUserService = authenticateUserService;
        this.jwtTokenService = jwtTokenService;
        this.refreshSessions = refreshSessions;
        this.secureRefreshCookie = secureRefreshCookie;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description = "Valida email e senha e retorna um access token JWT Bearer."
    )
    @SecurityRequirements
    @ApiResponse(
        responseCode = "200",
        description = "Credenciais válidas",
        content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Request inválido",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Credenciais inválidas",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedUser authenticatedUser = authenticateUserService.authenticate(
            new AuthenticateUserCommand(request.email(), request.password())
        );
        LoginTokenResponse tokenResponse = jwtTokenService.issueAccessToken(authenticatedUser);

        if (refreshSessions == null) return ResponseEntity.ok(new LoginResponse(tokenResponse.accessToken(), tokenResponse.tokenType(), tokenResponse.expiresIn()));
        String refresh = refreshSessions.issue(authenticatedUser);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.issue(refresh, java.time.Duration.ofDays(30), secureRefreshCookie).toString())
            .body(new LoginResponse(tokenResponse.accessToken(), tokenResponse.tokenType(), tokenResponse.expiresIn()));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String raw = cookie(request);
        AuthenticatedUser user = refreshSessions.rotate(raw);
        LoginTokenResponse token = jwtTokenService.issueAccessToken(user);
        String next = refreshSessions.issue(user);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.issue(next, java.time.Duration.ofDays(30), secureRefreshCookie).toString()).body(new LoginResponse(token.accessToken(), token.tokenType(), token.expiresIn()));
    }

    @PostMapping("/logout")
    @SecurityRequirements
    public ResponseEntity<Void> logout(HttpServletRequest request) { refreshSessions.revoke(cookie(request)); return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, RefreshTokenCookie.clear(secureRefreshCookie).toString()).build(); }

    private static String cookie(HttpServletRequest request){if(request.getCookies()==null)return null;for(Cookie c:request.getCookies())if(RefreshTokenCookie.NAME.equals(c.getName()))return c.getValue();return null;}

    @PostMapping("/register")
    @Operation(
        summary = "Cadastrar usuário",
        description = "Cria uma nova conta de usuário com email e senha. Não realiza login automático."
    )
    @SecurityRequirements
    @ApiResponse(
        responseCode = "201",
        description = "Usuário cadastrado com sucesso",
        content = @Content(schema = @Schema(implementation = RegisteredUserResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Request inválido ou senha fora da política",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "409",
        description = "Email já cadastrado",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Erro inesperado",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class))
    )
    public ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserResult result = registerUserService.register(
            new RegisterUserCommand(request.email(), request.password())
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new RegisteredUserResponse(result.id(), result.email(), result.createdAt()));
    }
}
