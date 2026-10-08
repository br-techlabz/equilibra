package br.com.equilibra.report.category.api;

import br.com.equilibra.report.category.application.CategoryReportQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/reports/categories")
@Tag(name="Category reports",description="Relatório financeiro agrupado por categoria")
@SecurityRequirement(name="bearerAuth")
public class CategoryReportController {
 private final CategoryReportQueryService service;
 public CategoryReportController(CategoryReportQueryService service){this.service=service;}
 @GetMapping
 @Operation(summary="Consultar relatório por categoria")
 public ResponseEntity<CategoryReportResponse> query(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to,@RequestParam(required=false) List<String> accountIds){return ResponseEntity.ok(service.query(from,to,accountIds));}
}
