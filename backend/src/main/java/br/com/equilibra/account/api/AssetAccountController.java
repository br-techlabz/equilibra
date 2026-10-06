package br.com.equilibra.account.api;

import br.com.equilibra.account.application.AssetAccountService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;

@RestController
@RequestMapping("/asset-accounts")
@Tag(name = "Asset accounts", description = "Contas de ativo do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class AssetAccountController {

    private final AssetAccountService service;
    private final br.com.equilibra.account.application.AccountBalanceQueryService balances;

    public AssetAccountController(AssetAccountService service, br.com.equilibra.account.application.AccountBalanceQueryService balances) {
        this.service = service;
        this.balances = balances;
    }

    @PostMapping
    @Operation(summary = "Criar conta de ativo")
    public ResponseEntity<AssetAccountResponse> create(@Valid @RequestBody CreateAssetAccountRequest request) {
        AssetAccountResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar contas de ativo")
    public ResponseEntity<List<AssetAccountResponse>> list(
        @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return ResponseEntity.ok(balances.list(includeInactive));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar conta de ativo")
    public ResponseEntity<AssetAccountResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta de ativo")
    public ResponseEntity<AssetAccountResponse> update(
        @PathVariable String id,
        @Valid @RequestBody UpdateAssetAccountRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar conta de ativo")
    public ResponseEntity<AssetAccountResponse> deactivate(@PathVariable String id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reativar conta de ativo")
    public ResponseEntity<AssetAccountResponse> activate(@PathVariable String id) {
        return ResponseEntity.ok(service.activate(id));
    }
}
