package br.com.equilibra.category.api;

import br.com.equilibra.category.application.CategoryService;
import br.com.equilibra.category.domain.CategoryApplicability;
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
import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Categorias financeiras do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar categoria")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar categorias")
    @Parameter(name = "includeInactive", in = ParameterIn.QUERY, description = "Inclui categorias inativas; padrão false", schema = @Schema(type = "boolean", defaultValue = "false"))
    @Parameter(name = "applicability", in = ParameterIn.QUERY, description = "Contexto de uso: EXPENSE inclui EXPENSE+BOTH; INCOME inclui INCOME+BOTH", schema = @Schema(implementation = CategoryApplicability.class))
    public ResponseEntity<List<CategoryResponse>> list(
        @RequestParam(defaultValue = "false") boolean includeInactive,
        @RequestParam(required = false) CategoryApplicability applicability
    ) {
        return ResponseEntity.ok(service.list(includeInactive, applicability));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria")
    public ResponseEntity<CategoryResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<CategoryResponse> update(
        @PathVariable String id,
        @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar categoria")
    public ResponseEntity<CategoryResponse> deactivate(@PathVariable String id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reativar categoria")
    public ResponseEntity<CategoryResponse> activate(@PathVariable String id) {
        return ResponseEntity.ok(service.activate(id));
    }
}
