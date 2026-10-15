package br.com.equilibra.report.export;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.io.FileOutputStream;
import static org.assertj.core.api.Assertions.assertThat;

class PdfGenerationTest {
    private final ReportExportRenderer renderer = new ReportExportRenderer();

    @Test
    void generateSampleFinancialReportPdf() throws Exception {
        List<ReportExportRenderer.DetailRow> details = List.of(
            new ReportExportRenderer.DetailRow("1", "2026-10-01T10:00:00Z", "Salário", "INCOME", "Receita", new BigDecimal("5000.00"), "Salário", "Conta Corrente", null),
            new ReportExportRenderer.DetailRow("2", "2026-10-02T14:30:00Z", "Supermercado", "EXPENSE", "Despesa", new BigDecimal("350.75"), "Alimentação", "Conta Corrente", null),
            new ReportExportRenderer.DetailRow("3", "2026-10-05T09:15:00Z", "Transferência para Poupança", "TRANSFER", "Transferência", new BigDecimal("1000.00"), null, "Conta Corrente", "Poupança"),
            new ReportExportRenderer.DetailRow("4", "2026-10-10T16:45:00Z", "Freelance", "INCOME", "Receita", new BigDecimal("1500.00"), "Trabalho Extra", null, "Conta Corrente"),
            new ReportExportRenderer.DetailRow("5", "2026-10-15T11:20:00Z", "Combustível", "EXPENSE", "Despesa", new BigDecimal("280.00"), "Transporte", "Conta Corrente", null)
        );

        ReportExportRenderer.FinancialReportData data = new ReportExportRenderer.FinancialReportData(
            Instant.parse("2026-10-01T00:00:00Z"),
            Instant.parse("2026-10-31T23:59:59Z"),
            List.of("Conta Corrente", "Poupança"),
            new BigDecimal("2500.00"),
            new BigDecimal("6500.00"),
            new BigDecimal("630.75"),
            new BigDecimal("5869.25"),
            new BigDecimal("0.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("1000.00"),
            new BigDecimal("4869.25"),
            new BigDecimal("7369.25"),
            details
        );

        byte[] pdf = renderer.pdfFinancialReport(data);
        try (FileOutputStream fos = new FileOutputStream("/tmp/equilibra-financeiro-teste2.pdf")) {
            fos.write(pdf);
        }
        assertThat(pdf).startsWith('%', 'P', 'D', 'F').hasSizeGreaterThan(1000);
        System.out.println("PDF gerado: " + pdf.length + " bytes");
    }
}
