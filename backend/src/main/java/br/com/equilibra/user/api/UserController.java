package br.com.equilibra.user.api;

import br.com.equilibra.user.application.GetCurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint autenticado para obter informações do usuário atual.
 */
@RestController
@RequestMapping({"/users", "/api/users"})
@Tag(name = "Users", description = "Endpoints relacionados ao usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final GetCurrentUserService getCurrentUserService;

    public UserController(GetCurrentUserService getCurrentUserService) {
        this.getCurrentUserService = getCurrentUserService;
    }

    @GetMapping("/me")
    @Operation(
        summary = "Obter usuário autenticado",
        description = "Retorna informações básicas do usuário autenticado via JWT Bearer token."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Usuário autenticado",
        content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Token ausente, inválido ou expirado",
        content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Usuário autenticado não encontrado no banco",
        content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
    )
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        CurrentUserResponse response = getCurrentUserService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}