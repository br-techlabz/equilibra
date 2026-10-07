package br.com.equilibra.report.api;

import br.com.equilibra.report.application.FinancialReportQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/reports/financial")
@Tag(name = "Financial reports", description = "Relatório financeiro derivado do ledger")
@SecurityRequirement(name = "bearerAuth")
public class FinancialReportController {
    private final FinancialReportQueryService service;
    public FinancialReportController(FinancialReportQueryService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Consultar relatório financeiro")
    @Parameter(name = "from", in = ParameterIn.QUERY, required = true)
    @Parameter(name = "to", in = ParameterIn.QUERY, required = true)
    public ResponseEntity<FinancialReportResponse> query(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(required = false) List<String> accountIds,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) { return ResponseEntity.ok(service.query(from, to, accountIds, page, size)); }
}
