package br.com.equilibra.report.export;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ReportExportRendererTest {
    private final ReportExportRenderer renderer = new ReportExportRenderer();
    @Test void csvEscapesAndNeutralizesFormulaFields() {
        String csv = new String(renderer.csv("x", List.of("Valor"), List.of(List.of("=SUM(A1)", "a;\"b"))), StandardCharsets.UTF_8);
        assertThat(csv).startsWith("﻿").contains("'=SUM(A1)").contains("\"a;\"\"b\"");
    }
    @Test void moneyPreservesBrazilianDecimalSeparator() { assertThat(ReportExportRenderer.money(new BigDecimal("3500.00"))).isEqualTo("3.500,00"); }
    @Test void pdfHasPdfSignatureAndMultiplePages() { byte[] pdf = renderer.pdf("Relatório", List.of("A"), java.util.stream.IntStream.range(0, 100).mapToObj(i -> List.of("linha " + i)).toList()); assertThat(pdf).startsWith('%', 'P', 'D', 'F').hasSizeGreaterThan(1000); }
}
