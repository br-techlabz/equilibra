package br.com.equilibra.creditcard.api;

import br.com.equilibra.creditcard.application.CreditCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/credit-cards")
@Tag(name = "Credit cards", description = "Cartões de crédito do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class CreditCardController {
    private final CreditCardService service;
    public CreditCardController(CreditCardService service) { this.service = service; }

    @PostMapping @Operation(summary = "Criar cartão de crédito")
    public ResponseEntity<CreditCardResponse> create(@Valid @RequestBody CreateCreditCardRequest request) {
        CreditCardResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping @Operation(summary = "Listar cartões de crédito")
    public ResponseEntity<List<CreditCardResponse>> list(@RequestParam(defaultValue = "false") boolean includeInactive) { return ResponseEntity.ok(service.list(includeInactive)); }
    @GetMapping("/{id}") @Operation(summary = "Consultar cartão de crédito")
    public ResponseEntity<CreditCardResponse> get(@PathVariable String id) { return ResponseEntity.ok(service.get(id)); }
    @PutMapping("/{id}") @Operation(summary = "Atualizar cartão de crédito")
    public ResponseEntity<CreditCardResponse> update(@PathVariable String id, @Valid @RequestBody UpdateCreditCardRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @PatchMapping("/{id}/deactivate") @Operation(summary = "Desativar cartão de crédito")
    public ResponseEntity<CreditCardResponse> deactivate(@PathVariable String id) { return ResponseEntity.ok(service.deactivate(id)); }
    @PatchMapping("/{id}/activate") @Operation(summary = "Reativar cartão de crédito")
    public ResponseEntity<CreditCardResponse> activate(@PathVariable String id) { return ResponseEntity.ok(service.activate(id)); }
}
