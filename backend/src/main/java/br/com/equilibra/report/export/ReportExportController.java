package br.com.equilibra.report.export;

import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.report.api.FinancialReportResponse;
import br.com.equilibra.report.api.FinancialReportTransaction;
import br.com.equilibra.report.application.FinancialReportQueryService;
import br.com.equilibra.report.category.api.CategoryReportResponse;
import br.com.equilibra.report.category.application.CategoryReportQueryService;
import br.com.equilibra.report.audit.api.AuditReportResponse;
import br.com.equilibra.report.audit.application.AuditReportQueryService;
import br.com.equilibra.transaction.domain.TransactionStatus;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.api.CurrentUser;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/reports")
@Tag(name = "Report exports")
@SecurityRequirement(name = "bearerAuth")
public class ReportExportController {
 private static final int MAX_EXPORT_ROWS = 5000;
 private static final String EXPORT_LIMIT_MESSAGE = "Export exceeds the maximum of 5000 records.";
 private final FinancialReportQueryService financial;
 private final CategoryReportQueryService category;
 private final AuditReportQueryService audit;
 private final ReportExportRenderer renderer;
 private final ProfessionalReportRenderer professionalRenderer;
 private final AssetAccountRepository accounts;
 private final CategoryRepository categories;
 private final CurrentUser currentUser;

 public ReportExportController(FinancialReportQueryService financial,
                               CategoryReportQueryService category,
                               AuditReportQueryService audit,
                               ReportExportRenderer renderer,
                               ProfessionalReportRenderer professionalRenderer,
                               AssetAccountRepository accounts,
                               CategoryRepository categories,
                               CurrentUser currentUser) {
     this.financial = financial;
     this.category = category;
     this.audit = audit;
     this.renderer = renderer;
     this.professionalRenderer = professionalRenderer;
     this.accounts = accounts;
     this.categories = categories;
     this.currentUser = currentUser;
 }
 @GetMapping("/financial/export")
    public ResponseEntity<byte[]> financial(@RequestParam ExportFormat format,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                            @RequestParam(required = false) List<String> accountIds) {
        FinancialReportResponse r = financial.query(from, to, accountIds, 0, 100);

        // Build structured data for the new PDF layout
        List<ProfessionalReportRenderer.DetailRow> details = r.details().content().stream()
            .map(x -> new ProfessionalReportRenderer.DetailRow(
                x.id(),
                x.occurredAt().toString(),
                x.description(),
                x.type().name(),
                typeLabel(x.type()),
                x.amount(),
                categoryName(x.categoryId()),
                accountName(x.sourceAccountId()),
                accountName(x.destinationAccountId())
            ))
            .toList();

        List<String> accountNames = r.accounts().stream()
            .map(FinancialReportResponse.AccountSelection::name)
            .toList();

        ProfessionalReportRenderer.FinancialReportData pdfData = new ProfessionalReportRenderer.FinancialReportData(
            r.period().from(),
            r.period().to(),
            accountNames,
            r.openingBalance(),
            r.incomeTotal(),
            r.expenseTotal(),
            r.financialResult(),
            r.incomingTransferTotal(),
            r.outgoingTransferTotal(),
            r.internalTransferTotal(),
            r.balanceChange(),
            r.closingBalance(),
            details
        );

        byte[] csv = renderer.csv("Relatório financeiro",
            List.of("Indicador", "Valor", "Tipo", "Data", "Conta origem", "Conta destino", "Categoria"),
            buildCsvRows(r));
        byte[] pdf = professionalRenderer.pdfFinancialReport(pdfData);

        return response(format, "financeiro", csv, pdf);
    }

    private String typeLabel(TransactionType type) {
        return switch (type) {
            case EXPENSE -> "Despesa";
            case INCOME -> "Receita";
            case TRANSFER -> "Transferência";
            case STATEMENT_PAYMENT -> "Pagamento de Fatura";
        };
    }

    private String statusLabel(TransactionStatus status) {
        return switch (status) {
            case ACTIVE -> "Ativo";
            case CANCELLED -> "Cancelada";
        };
    }

    private String applicabilityLabel(CategoryApplicability applicability) {
        return switch (applicability) {
            case EXPENSE -> "Despesa";
            case INCOME -> "Receita";
            case BOTH -> "Ambas";
        };
    }

    private List<List<String>> buildCsvRows(FinancialReportResponse r) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Saldo inicial", ReportExportRenderer.money(r.openingBalance()), "", "", "", "", ""));
        rows.add(List.of("Receitas", ReportExportRenderer.money(r.incomeTotal()), "", "", "", "", ""));
        rows.add(List.of("Despesas", ReportExportRenderer.money(r.expenseTotal()), "", "", "", "", ""));
        rows.add(List.of("Resultado", ReportExportRenderer.money(r.financialResult()), "", "", "", "", ""));
        rows.add(List.of("Transferências recebidas", ReportExportRenderer.money(r.incomingTransferTotal()), "", "", "", "", ""));
        rows.add(List.of("Transferências enviadas", ReportExportRenderer.money(r.outgoingTransferTotal()), "", "", "", "", ""));
        rows.add(List.of("Transferências internas", ReportExportRenderer.money(r.internalTransferTotal()), "", "", "", "", ""));
        rows.add(List.of("Variação", ReportExportRenderer.money(r.balanceChange()), "", "", "", "", ""));
        rows.add(List.of("Saldo final", ReportExportRenderer.money(r.closingBalance()), "", "", "", "", ""));
        rows.add(List.of("", ""));
        rows.add(List.of("Data", "Descrição", "Tipo", "Conta origem", "Conta destino", "Categoria", "Valor"));
        rows.addAll(r.details().content().stream()
            .map(x -> List.of(
                ReportExportRenderer.date(x.occurredAt().toString()),
                x.description(),
                typeLabel(x.type()),
                accountName(x.sourceAccountId()),
                accountName(x.destinationAccountId()),
                categoryName(x.categoryId()),
                ReportExportRenderer.money(x.amount())
            ))
            .toList());
        return rows;
    }
 @GetMapping("/categories/export")
    public ResponseEntity<byte[]> categories(@RequestParam ExportFormat format,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                             @RequestParam(required = false) List<String> accountIds) {
        CategoryReportResponse r = category.query(from, to, accountIds);

        List<ProfessionalReportRenderer.CategoryRow> categories = r.categories().stream()
            .map(c -> new ProfessionalReportRenderer.CategoryRow(
                c.categoryName(),
                applicabilityLabel(c.applicability()),
                c.incomeTotal(),
                c.expenseTotal(),
                c.netResult()
            ))
            .toList();

        List<String> accountNames = new ArrayList<>(); // Not easily available here without extra query

        ProfessionalReportRenderer.CategoryReportData pdfData = new ProfessionalReportRenderer.CategoryReportData(
            from,
            to,
            accountNames,
            r.incomeTotal(),
            r.expenseTotal(),
            r.netResult(),
            categories
        );

        byte[] csv = renderer.csv("Relatório por categoria",
            List.of("Categoria", "Receitas", "Despesas", "Resultado"),
            r.categories().stream()
                .map(c -> List.of(c.categoryName(), ReportExportRenderer.money(c.incomeTotal()), ReportExportRenderer.money(c.expenseTotal()), ReportExportRenderer.money(c.netResult())))
                .toList());
        byte[] pdf = professionalRenderer.pdfCategoryReport(pdfData);

        return response(format, "categorias", csv, pdf);
    }
 @GetMapping("/audit/export")
    public ResponseEntity<byte[]> audit(@RequestParam ExportFormat format,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                        @RequestParam(required = false) List<String> accountIds,
                                        @RequestParam(required = false) TransactionType type,
                                        @RequestParam(required = false) TransactionStatus status,
                                        @RequestParam(required = false) String categoryId,
                                        @RequestParam(required = false) List<String> tagIds) {
        List<AuditReportResponse.AuditTransaction> all = new ArrayList<>();
        int page = 0;
        AuditReportResponse r;
        do {
            r = audit.list(from, to, accountIds, type, status, categoryId, tagIds, page, 100);
            all.addAll(r.content());
            if (all.size() > MAX_EXPORT_ROWS) throw new IllegalArgumentException(EXPORT_LIMIT_MESSAGE);
            page++;
        } while (page < r.totalPages());

        List<ProfessionalReportRenderer.AuditRow> auditRows = all.stream()
            .map(x -> new ProfessionalReportRenderer.AuditRow(
                x.occurredAt().toString(),
                typeLabel(x.type()),
                statusLabel(x.status()),
                x.description(),
                x.amount(),
                accountName(x.sourceAccountId()),
                categoryName(x.categoryId())
            ))
            .toList();

        BigDecimal totalVolume = all.stream()
            .map(AuditReportResponse.AuditTransaction::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        ProfessionalReportRenderer.AuditReportData pdfData = new ProfessionalReportRenderer.AuditReportData(
            from,
            to,
            new ArrayList<>(),
            all.size(),
            totalVolume,
            auditRows
        );

        List<List<String>> rows = all.stream()
            .map(x -> List.of(
                x.id(),
                typeLabel(x.type()),
                statusLabel(x.status()),
                x.description(),
                ReportExportRenderer.money(x.amount()),
                ReportExportRenderer.date(x.occurredAt().toString()),
                ReportExportRenderer.date(x.createdAt().toString()),
                ReportExportRenderer.date(x.updatedAt().toString()),
                accountName(x.sourceAccountId()),
                accountName(x.destinationAccountId()),
                categoryName(x.categoryId()),
                x.tags().stream().map(t -> t.name()).reduce((a, b) -> a + ", ").orElse(""),
                String.valueOf(x.attachmentCount())
            ))
            .toList();

        byte[] csv = renderer.csv("Relatório de auditoria",
            List.of("ID", "Tipo", "Status", "Descrição", "Valor", "Ocorrida em", "Criada em", "Atualizada em", "Conta origem", "Conta destino", "Categoria", "Tags", "Anexos"),
            rows);
        byte[] pdf = professionalRenderer.pdfAuditReport(pdfData);

        return response(format, "auditoria", csv, pdf);
    }
 private String accountName(String id){ if(id==null)return ""; return accounts.findByIdAndOwnerId(id,currentUser.id().toString()).map(a->a.getName()).orElse("Conta indisponível"); }
 private String categoryName(String id){ if(id==null)return "Sem categoria"; return categories.findByIdAndOwnerId(id,currentUser.id().toString()).map(c->c.getName()).orElse("Categoria indisponível"); }
 private ResponseEntity<byte[]> response(ExportFormat format,String name,byte[] csv,byte[] pdf){boolean isPdf=format==ExportFormat.PDF; MediaType type=isPdf?MediaType.APPLICATION_PDF:MediaType.parseMediaType("text/csv; charset=UTF-8"); return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noStore()).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=equilibra-"+name+"."+(isPdf?"pdf":"csv")).body(isPdf?pdf:csv);}
}
