package br.com.equilibra.transaction.api;

import br.com.equilibra.transaction.application.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/expenses")
@Tag(name = "Expenses", description = "Despesas do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService service;

    public ExpenseController(ExpenseService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Criar despesa")
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody CreateExpenseRequest request) {
        ExpenseResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar despesas")
    @Parameter(name = "page", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0"))
    @Parameter(name = "size", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20", maximum = "100"))
    public ResponseEntity<ExpensePageResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "false") boolean includeCancelled
    ) { return ResponseEntity.ok(service.list(page, size, includeCancelled)); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar despesa")
    public ResponseEntity<ExpenseResponse> get(@PathVariable String id) { return ResponseEntity.ok(service.get(id)); }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar despesa")
    public ResponseEntity<ExpenseResponse> update(@PathVariable String id, @Valid @RequestBody UpdateExpenseRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar despesa")
    public ResponseEntity<ExpenseResponse> cancel(@PathVariable String id) { return ResponseEntity.ok(service.cancel(id)); }
}
