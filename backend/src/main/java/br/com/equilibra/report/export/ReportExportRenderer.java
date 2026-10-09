package br.com.equilibra.report.export;

import org.springframework.stereotype.Component;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class ReportExportRenderer {
    public byte[] csv(String title, List<String> headers, List<List<String>> rows) {
        StringBuilder out = new StringBuilder("﻿");
        out.append(headers.stream().map(this::cell).reduce((a,b)->a+";"+b).orElse("")).append('\n');
        for (List<String> row : rows) out.append(row.stream().map(this::cell).reduce((a,b)->a+";"+b).orElse("")).append('\n');
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }
    public byte[] pdf(String title, List<String> headers, List<List<String>> rows) {
        return pdf(title, "Período aplicado", "Filtros aplicados", headers, rows);
    }
    public byte[] pdf(String title, String period, String filters, List<String> headers, List<List<String>> rows) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int index = 0;
            while (index < Math.max(rows.size(), 1)) {
                PDPage page = new PDPage(); document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText(); stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10); stream.newLineAtOffset(40, 750); stream.showText(safe(title)); stream.newLineAtOffset(0, -16); stream.showText(safe(period)); stream.newLineAtOffset(0, -16); stream.showText(safe(filters)); stream.newLineAtOffset(0, -20); stream.showText(safe(String.join(" | ", headers))); stream.newLineAtOffset(0, -16);
                    int end = Math.min(index + 45, rows.size());
                    for (; index < end; index++) { stream.showText(safe(String.join(" | ", rows.get(index)))); stream.newLineAtOffset(0, -14); }
                    stream.endText();
                }
            }
            document.save(output); return output.toByteArray();
        } catch (IOException e) { throw new IllegalStateException("Could not generate PDF", e); }
    }
    private String safe(String value) { return value.replaceAll("[^\\x20-\\x7E]", "?"); }
    private String cell(String raw) {
        String value = raw == null ? "" : raw.replace("\r", " ").replace("\n", " ");
        if (value.matches("^[=+\\-@\\t].*")) value = "'" + value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }
    public static String money(BigDecimal value) { return value == null ? "0.00" : value.toPlainString(); }
    public static String date(Instant value) { return value == null ? "" : value.toString(); }
}
