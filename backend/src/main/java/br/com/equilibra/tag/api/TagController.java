package br.com.equilibra.tag.api;

import br.com.equilibra.tag.application.TagService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tags")
@Tag(name = "Tags", description = "Tags privadas do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class TagController {
    private final TagService service;
    public TagController(TagService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping
    public ResponseEntity<List<TagResponse>> list(@RequestParam(defaultValue = "false") boolean includeInactive) { return ResponseEntity.ok(service.list(includeInactive)); }
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> get(@PathVariable String id) { return ResponseEntity.ok(service.get(id)); }
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(@PathVariable String id, @Valid @RequestBody UpdateTagRequest request) { return ResponseEntity.ok(service.update(id, request)); }
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<TagResponse> deactivate(@PathVariable String id) { return ResponseEntity.ok(service.deactivate(id)); }
    @PatchMapping("/{id}/activate")
    public ResponseEntity<TagResponse> activate(@PathVariable String id) { return ResponseEntity.ok(service.activate(id)); }
}
