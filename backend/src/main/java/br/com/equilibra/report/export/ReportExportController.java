package br.com.equilibra.report.export;

import br.com.equilibra.report.api.FinancialReportResponse;
import br.com.equilibra.report.application.FinancialReportQueryService;
import br.com.equilibra.report.category.api.CategoryReportResponse;
import br.com.equilibra.report.category.application.CategoryReportQueryService;
import br.com.equilibra.report.audit.api.AuditReportResponse;
import br.com.equilibra.report.audit.application.AuditReportQueryService;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/reports")
@Tag(name = "Report exports")
@SecurityRequirement(name = "bearerAuth")
public class ReportExportController {
 private static final int MAX_EXPORT_ROWS = 5000;
 private final FinancialReportQueryService financial; private final CategoryReportQueryService category; private final AuditReportQueryService audit; private final ReportExportRenderer renderer;
 public ReportExportController(FinancialReportQueryService financial, CategoryReportQueryService category, AuditReportQueryService audit, ReportExportRenderer renderer){this.financial=financial;this.category=category;this.audit=audit;this.renderer=renderer;}
 @GetMapping("/financial/export") public ResponseEntity<byte[]> financial(@RequestParam ExportFormat format,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,@RequestParam(required=false) List<String> accountIds){ FinancialReportResponse r=financial.query(from,to,accountIds,0,100); return response(format,"financeiro",renderer.csv("Relatório financeiro",List.of("Indicador","Valor"),List.of(List.of("Saldo inicial",ReportExportRenderer.money(r.openingBalance())),List.of("Receitas",ReportExportRenderer.money(r.incomeTotal())),List.of("Despesas",ReportExportRenderer.money(r.expenseTotal())),List.of("Saldo final",ReportExportRenderer.money(r.closingBalance())))),renderer.pdf("Relatório financeiro",List.of("Indicador","Valor"),List.of(List.of("Saldo inicial",ReportExportRenderer.money(r.openingBalance())),List.of("Receitas",ReportExportRenderer.money(r.incomeTotal())),List.of("Despesas",ReportExportRenderer.money(r.expenseTotal())),List.of("Saldo final",ReportExportRenderer.money(r.closingBalance()))))); }
 @GetMapping("/categories/export") public ResponseEntity<byte[]> categories(@RequestParam ExportFormat format,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,@RequestParam(required=false) List<String> accountIds){ CategoryReportResponse r=category.query(from,to,accountIds); List<List<String>> rows=r.categories().stream().map(c->List.of(c.categoryName(),ReportExportRenderer.money(c.incomeTotal()),ReportExportRenderer.money(c.expenseTotal()),ReportExportRenderer.money(c.netResult()))).toList(); return response(format,"categorias",renderer.csv("Relatório por categoria",List.of("Categoria","Receitas","Despesas","Resultado"),rows),renderer.pdf("Relatório por categoria",List.of("Categoria","Receitas","Despesas","Resultado"),rows)); }
 @GetMapping("/audit/export") public ResponseEntity<byte[]> audit(@RequestParam ExportFormat format,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,@RequestParam(required=false) List<String> accountIds,@RequestParam(required=false) TransactionType type,@RequestParam(required=false) TransactionStatus status,@RequestParam(required=false) String categoryId,@RequestParam(required=false) List<String> tagIds){ List<AuditReportResponse.AuditTransaction> all = new ArrayList<>(); int page = 0; AuditReportResponse r; do { r=audit.list(from,to,accountIds,type,status,categoryId,tagIds,page,100); all.addAll(r.content()); if (all.size() > MAX_EXPORT_ROWS) throw new IllegalArgumentException("Export exceeds the maximum of " + MAX_EXPORT_ROWS + " records."); page++; } while (page < r.totalPages()); List<List<String>> rows=all.stream().map(x->List.of(x.id(),x.type().name(),x.status().name(),x.description(),ReportExportRenderer.money(x.amount()),ReportExportRenderer.date(x.occurredAt()),ReportExportRenderer.date(x.createdAt()),ReportExportRenderer.date(x.updatedAt()),String.valueOf(x.sourceAccountId()),String.valueOf(x.destinationAccountId()),String.valueOf(x.categoryId()),x.tags().stream().map(t->t.name()).reduce((a,b)->a+", ").orElse(""),String.valueOf(x.attachmentCount()))).toList(); return response(format,"auditoria",renderer.csv("Relatório de auditoria",List.of("ID","Tipo","Status","Descrição","Valor","Ocorrida em","Criada em","Atualizada em","Conta origem","Conta destino","Categoria","Tags","Anexos"),rows),renderer.pdf("Relatório de auditoria",List.of("ID","Tipo","Status","Descrição","Valor","Ocorrida em","Criada em","Atualizada em","Conta origem","Conta destino","Categoria","Tags","Anexos"),rows)); }
 private ResponseEntity<byte[]> response(ExportFormat format,String name,byte[] csv,byte[] pdf){boolean isPdf=format==ExportFormat.PDF; MediaType type=isPdf?MediaType.APPLICATION_PDF:MediaType.parseMediaType("text/csv; charset=UTF-8"); return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=equilibra-"+name+"."+(isPdf?"pdf":"csv")).body(isPdf?pdf:csv);}
}
