package br.com.equilibra.commitment.api;

import br.com.equilibra.commitment.application.FinancialCommitmentService;
import br.com.equilibra.commitment.domain.CommitmentStatus;
import br.com.equilibra.commitment.domain.CommitmentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/commitments")
@Tag(name="Financial Commitments")
@SecurityRequirement(name="bearerAuth")
public class FinancialCommitmentController {
 private final FinancialCommitmentService service;
 public FinancialCommitmentController(FinancialCommitmentService service){this.service=service;}
 @Operation(summary="Create a pending financial commitment")
 @PostMapping public ResponseEntity<CommitmentDtos.Response> create(@Valid @RequestBody CommitmentDtos.Create request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
 @Operation(summary="Generate recurring commitments on demand")
 @PostMapping("/generate") public List<CommitmentDtos.Response> generate(@Valid @RequestBody CommitmentDtos.Generate request){return service.generate(request);}
 @Operation(summary="List commitments with owner-scoped filters and pagination")
 @GetMapping public Page<CommitmentDtos.Response> list(@RequestParam(required=false) CommitmentType type,@RequestParam(required=false) CommitmentStatus status,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(required=false) String accountId,@RequestParam(required=false) String categoryId,@RequestParam(required=false) String recurrenceRuleId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(new CommitmentDtos.Filter(type,status,from,to,accountId,categoryId,recurrenceRuleId),PageRequest.of(page,size,Sort.by("dueDate").ascending().and(Sort.by("id").ascending())));}
 @Operation(summary="Get one owner-scoped commitment")
 @GetMapping("/{id}") public CommitmentDtos.Response get(@PathVariable String id){return service.get(id);}
 @Operation(summary="Edit a pending commitment")
 @PutMapping("/{id}") public CommitmentDtos.Response update(@PathVariable String id,@Valid @RequestBody CommitmentDtos.Update request){return service.update(id,request);}
 @Operation(summary="Cancel a pending commitment")
 @PostMapping("/{id}/cancel") public CommitmentDtos.Response cancel(@PathVariable String id){return service.cancel(id);}
 @Operation(summary="Settle a commitment into one real financial transaction")
 @PostMapping("/{id}/settle") public CommitmentDtos.Response settle(@PathVariable String id,@Valid @RequestBody CommitmentDtos.Settle request){return service.settle(id,request);}
}
