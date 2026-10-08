package br.com.equilibra.report.audit.api;
import br.com.equilibra.report.audit.application.AuditReportQueryService;
import br.com.equilibra.transaction.domain.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;import java.util.List;
@RestController @RequestMapping("/reports/audit") @Tag(name="Audit reports",description="Auditoria de transações financeiras") @SecurityRequirement(name="bearerAuth") public class AuditReportController{private final AuditReportQueryService service;public AuditReportController(AuditReportQueryService service){this.service=service;}@GetMapping public ResponseEntity<AuditReportResponse> list(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant to,@RequestParam(required=false)List<String> accountIds,@RequestParam(required=false)TransactionType type,@RequestParam(required=false)TransactionStatus status,@RequestParam(required=false)String categoryId,@RequestParam(required=false)List<String> tagIds,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ResponseEntity.ok(service.list(from,to,accountIds,type,status,categoryId,tagIds,page,size));}}
