package br.com.equilibra.report.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class ReportExportRenderer {

    private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
    private static final float PAGE_WIDTH = PAGE_SIZE.getWidth();
    private static final float PAGE_HEIGHT = PAGE_SIZE.getHeight();
    private static final float MARGIN = 48f;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float HEADER_HEIGHT = 80f;
    private static final float FOOTER_HEIGHT = 30f;

    // Colors matching Equilibra Design System
    private static final Color PRIMARY_DARK = new Color(0x4C, 0x1D, 0x95); // #4c1d95
    private static final Color PRIMARY = new Color(0x7C, 0x3A, 0xED); // #7c3aed
    private static final Color PRIMARY_LIGHT = new Color(0xED, 0xE9, 0xFE); // #ede9fe
    private static final Color PRIMARY_50 = new Color(0xF5, 0xF3, 0xFF); // #f5f3ff
    private static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A); // #0f172a
    private static final Color TEXT_SECONDARY = new Color(0x33, 0x41, 0x55); // #334155
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B); // #64748b
    private static final Color TEXT_ON_PRIMARY = Color.WHITE;
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(0xE2, 0xE8, 0xF0); // #e2e8f0
    private static final Color CARD_SHADOW = new Color(0x0F, 0x17, 0x2A, 0x14); // rgba(15,23,42,0.08)
    private static final Color INCOME_COLOR = new Color(0x5B, 0x21, 0xB6); // #5b21b6
    private static final Color INCOME_BG = new Color(0xF5, 0xF3, 0xFF); // #f5f3ff
    private static final Color EXPENSE_COLOR = new Color(0xB8, 0x2E, 0x2E); // #b82e2e
    private static final Color EXPENSE_BG = new Color(0xFD, 0xF2, 0xF2); // #fdf2f2
    private static final Color TRANSFER_COLOR = new Color(0x0D, 0x47, 0xA1); // #0d47a1
    private static final Color TABLE_HEADER_BG = new Color(0xF8, 0xFA, 0xFC); // #f8fafc
    private static final Color TABLE_BORDER = new Color(0xE2, 0xE8, 0xF0); // #e2e8f0
    private static final Color TABLE_ALT_ROW = new Color(0xFA, 0xFA, 0xFC); // #fafafc
    private static final Color DIVIDER = new Color(0xE2, 0xE8, 0xF0); // #e2e8f0

    private PDFont fontRegular;
    private PDFont fontBold;
    private PDFont fontMono;

    public byte[] csv(String title, List<String> headers, List<List<String>> rows) {
        StringBuilder out = new StringBuilder("﻿");
        out.append(headers.stream().map(this::cell).reduce((a, b) -> a + ";" + b).orElse("")).append('\n');
        for (List<String> row : rows) {
            out.append(row.stream().map(this::cell).reduce((a, b) -> a + ";" + b).orElse("")).append('\n');
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] pdf(String title, List<String> headers, List<List<String>> rows) {
        return pdf(title, "Período aplicado", "Filtros aplicados", headers, rows);
    }

    public byte[] pdf(String title, String period, String filters, List<String> headers, List<List<String>> rows) {
        // Legacy method - kept for backward compatibility
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int index = 0;
            boolean firstPage = true;
            while (firstPage || index < rows.size()) {
                firstPage = false;
                PDPage page = new PDPage(); document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    stream.newLineAtOffset(40, 750);
                    stream.showText(safe(title));
                    stream.newLineAtOffset(0, -16);
                    stream.showText(safe(period));
                    stream.newLineAtOffset(0, -16);
                    stream.showText(safe(filters));
                    stream.newLineAtOffset(0, -20);
                    stream.showText(safe(String.join(" | ", headers)));
                    stream.newLineAtOffset(0, -16);
                    int end = Math.min(index + 45, rows.size());
                    for (; index < end; index++) {
                        stream.showText(safe(String.join(" | ", rows.get(index))));
                        stream.newLineAtOffset(0, -14);
                    }
                    stream.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not generate PDF", e);
        }
    }

    public byte[] pdfFinancialReport(FinancialReportData data) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            loadFonts(document);
            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            float y = PAGE_HEIGHT - MARGIN;
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                y = drawHeader(stream, y, data);
                y = drawSummaryCards(stream, y, data);
                y = drawTransfersSection(stream, y, data);
                y = drawDetailsTable(stream, y, data);
                drawFooter(stream);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not generate financial report PDF", e);
        }
    }

    public byte[] pdfCategoryReport(CategoryReportData data) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            loadFonts(document);
            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);

            float y = PAGE_HEIGHT - MARGIN;
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                y = drawCategoryHeader(stream, y, data);
                y = drawCategorySummaryCards(stream, y, data);
                y = drawCategoryTable(stream, y, data);
                drawFooter(stream);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not generate category report PDF", e);
        }
    }

    private void loadFonts(PDDocument document) throws IOException {
        // Use standard PDFBox fonts (always available)
        fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        fontMono = new PDType1Font(Standard14Fonts.FontName.COURIER);
    }

    // ========== Category Report Drawing Methods ==========

    private float drawCategoryHeader(PDPageContentStream stream, float y, CategoryReportData data) throws IOException {
        final float BAR_HEIGHT = 40f;
        final float BAR_TOP = y - 12;
        drawRect(stream, MARGIN, BAR_TOP - BAR_HEIGHT, CONTENT_WIDTH, BAR_HEIGHT, PRIMARY_DARK);

        drawText(stream, "Relatório por Categoria", MARGIN + 12, BAR_TOP - 10, 18, fontBold, TEXT_ON_PRIMARY);
        drawText(stream, "Equilibra - Distribuição de Receitas e Despesas", MARGIN + 12, BAR_TOP - 24, 9, fontRegular, new Color(0xC4, 0xB5, 0xFD));

        String periodText = formatPeriod(data.periodFrom(), data.periodTo());
        float periodWidth = fontRegular.getStringWidth(periodText) / 1000f * 9;
        drawText(stream, periodText, PAGE_WIDTH - MARGIN - periodWidth - 12, BAR_TOP - 10, 9, fontRegular, TEXT_ON_PRIMARY);

        String generatedText = "Gerado em " + formatDateTime(Instant.now());
        float genWidth = fontRegular.getStringWidth(generatedText) / 1000f * 8;
        drawText(stream, generatedText, PAGE_WIDTH - MARGIN - genWidth - 12, BAR_TOP - 22, 8, fontRegular, new Color(0xA7, 0x8B, 0xFA));

        if (data.accounts() != null && !data.accounts().isEmpty()) {
            String accountsText = "Contas: " + String.join(", ", data.accounts());
            float accWidth = fontRegular.getStringWidth(accountsText) / 1000f * 8;
            drawText(stream, accountsText, MARGIN + 12, BAR_TOP - 34, 8, fontRegular, new Color(0xA7, 0x8B, 0xFA));
        }

        float barBottom = BAR_TOP - BAR_HEIGHT;
        return barBottom - 10;
    }

    private float drawCategorySummaryCards(PDPageContentStream stream, float y, CategoryReportData data) throws IOException {
        float cardHeight = 56f;
        float cardWidth = (CONTENT_WIDTH - 32f) / 3f;
        float gap = 16f;
        float startX = MARGIN;

        // First row - 3 cards, shifted down by 100px (75pt at 72 DPI)
        float rowTop = y - 75f;  // 100px ≈ 75pt
        drawMetricCard(stream, startX, rowTop, cardWidth, cardHeight,
            "Total Receitas", formatMoney(data.incomeTotal()), INCOME_COLOR, true, false, false, true);
        drawMetricCard(stream, startX + cardWidth + gap, rowTop, cardWidth, cardHeight,
            "Total Despesas", formatMoney(data.expenseTotal()), EXPENSE_COLOR, false, true, false, true);
        drawMetricCard(stream, startX + 2 * (cardWidth + gap), rowTop, cardWidth, cardHeight,
            "Resultado", formatMoney(data.netResult()),
            data.netResult().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR,
            true, false, false, true);

        return rowTop - cardHeight - 12;
    }

    private float drawCategoryTable(PDPageContentStream stream, float y, CategoryReportData data) throws IOException {
        drawText(stream, "Categorias", MARGIN, y, 12, fontBold, TEXT_PRIMARY);
        drawText(stream, "Receitas e despesas separadas por categoria.", MARGIN, y - 16, 8, fontRegular, TEXT_MUTED);
        y -= 30;

        if (data.categories() == null || data.categories().isEmpty()) {
            drawText(stream, "Nenhuma categoria no período.", MARGIN + 16, y, 10, fontRegular, TEXT_MUTED);
            drawText(stream, "Não há receitas ou despesas para os filtros selecionados.", MARGIN + 16, y - 16, 9, fontRegular, TEXT_MUTED);
            return y - 40;
        }

        String[] columns = {"Categoria", "Aplicabilidade", "Receitas", "Despesas", "Resultado"};
        float[] colWidths = {180f, 100f, 120f, 120f, 120f};
        float rowHeight = 22f;
        float tableX = MARGIN;
        float tableWidth = CONTENT_WIDTH;

        float totalColWidth = 0;
        for (float w : colWidths) totalColWidth += w;
        if (totalColWidth > tableWidth) {
            float scale = tableWidth / totalColWidth;
            for (int i = 0; i < colWidths.length; i++) colWidths[i] *= scale;
        }

        float headerY = y;
        drawRect(stream, tableX, headerY - rowHeight, tableWidth, rowHeight, TABLE_HEADER_BG);
        drawLine(stream, tableX, headerY - rowHeight, tableX + tableWidth, headerY - rowHeight, TABLE_BORDER, 0.5f);

        float colX = tableX;
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) drawLine(stream, colX, headerY, colX, headerY - rowHeight, TABLE_BORDER, 0.5f);
            drawText(stream, columns[i], colX + 5, headerY - 13, 7.5f, fontBold, TEXT_SECONDARY);
            colX += colWidths[i];
        }
        drawLine(stream, colX, headerY, colX, headerY - rowHeight, TABLE_BORDER, 0.5f);

        y = headerY - rowHeight;

        for (int rowIndex = 0; rowIndex < data.categories().size(); rowIndex++) {
            CategoryRow row = data.categories().get(rowIndex);

            if (y - rowHeight < MARGIN + FOOTER_HEIGHT) break;

            if (rowIndex % 2 == 1) {
                drawRect(stream, tableX, y - rowHeight, tableWidth, rowHeight, TABLE_ALT_ROW);
            }

            drawLine(stream, tableX, y - rowHeight, tableX + tableWidth, y - rowHeight, TABLE_BORDER, 0.5f);

            colX = tableX;
            String[] values = {
                truncate(row.categoryName(), 35),
                row.applicability(),
                formatMoney(row.incomeTotal()),
                formatMoney(row.expenseTotal()),
                formatMoney(row.netResult())
            };

            for (int i = 0; i < values.length; i++) {
                if (i > 0) drawLine(stream, colX, y, colX, y - rowHeight, TABLE_BORDER, 0.5f);

                Color textColor = TEXT_PRIMARY;
                PDFont font = fontRegular;
                float fontSize = 6.5f;

                if (i == 2) { // Receitas
                    font = fontMono; textColor = INCOME_COLOR;
                } else if (i == 3) { // Despesas
                    font = fontMono; textColor = EXPENSE_COLOR;
                } else if (i == 4) { // Resultado
                    font = fontMono;
                    textColor = row.netResult().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;
                }

                drawText(stream, values[i], colX + 5, y - 13, fontSize, font, textColor);
                colX += colWidths[i];
            }
            drawLine(stream, colX, y, colX, y - rowHeight, TABLE_BORDER, 0.5f);
            y -= rowHeight;
        }

        drawRectOutline(stream, tableX, headerY - rowHeight, tableWidth, headerY - y, TABLE_BORDER, 0.5f);
        return y - 16;
    }

    private float drawHeader(PDPageContentStream stream, float y, FinancialReportData data) throws IOException {
        // Header background bar - compact
        final float BAR_HEIGHT = 40f;
        final float BAR_TOP = y - 12;  // Start 12pt below initial y
        drawRect(stream, MARGIN, BAR_TOP - BAR_HEIGHT, CONTENT_WIDTH, BAR_HEIGHT, PRIMARY_DARK);
        // Bar spans from BAR_TOP - BAR_HEIGHT to BAR_TOP (e.g., y-52 to y-12)

        // Title (inside bar)
        drawText(stream, "Relatório Financeiro", MARGIN + 12, BAR_TOP - 10, 18, fontBold, TEXT_ON_PRIMARY);

        // Subtitle
        drawText(stream, "Equilibra - Gerenciamento de Finanças Domésticas", MARGIN + 12, BAR_TOP - 24, 9, fontRegular, new Color(0xC4, 0xB5, 0xFD));

        // Period info on the right
        String periodText = formatPeriod(data.periodFrom(), data.periodTo());
        float periodWidth = fontRegular.getStringWidth(periodText) / 1000f * 9;
        drawText(stream, periodText, PAGE_WIDTH - MARGIN - periodWidth - 12, BAR_TOP - 10, 9, fontRegular, TEXT_ON_PRIMARY);

        // Generated date
        String generatedText = "Gerado em " + formatDateTime(Instant.now());
        float genWidth = fontRegular.getStringWidth(generatedText) / 1000f * 8;
        drawText(stream, generatedText, PAGE_WIDTH - MARGIN - genWidth - 12, BAR_TOP - 22, 8, fontRegular, new Color(0xA7, 0x8B, 0xFA));

        // Accounts info
        if (data.accounts() != null && !data.accounts().isEmpty()) {
            String accountsText = "Contas: " + String.join(", ", data.accounts());
            float accWidth = fontRegular.getStringWidth(accountsText) / 1000f * 8;
            drawText(stream, accountsText, MARGIN + 12, BAR_TOP - 34, 8, fontRegular, new Color(0xA7, 0x8B, 0xFA));
        }

        // Return position BELOW the header bar with a small gap
        float barBottom = BAR_TOP - BAR_HEIGHT;  // y - 52
        return barBottom - 10;  // y - 62 (10pt gap below bar)
    }

    private float drawSummaryCards(PDPageContentStream stream, float y, FinancialReportData data) throws IOException {
        float cardHeight = 56f;  // Reduced from 72f
        float cardWidth = (CONTENT_WIDTH - 32f) / 3f; // 3 cards with gaps (reduced gap)
        float gap = 16f;  // Reduced from 24f
        float startX = MARGIN;

        // First row - all cards at same Y (top aligned) - skip top border to avoid line from header
        float row1Top = y;
        drawMetricCard(stream, startX, row1Top, cardWidth, cardHeight,
            "Saldo Inicial", formatMoney(data.openingBalance()), TEXT_PRIMARY, false, false, false, true);
        drawMetricCard(stream, startX + cardWidth + gap, row1Top, cardWidth, cardHeight,
            "Receitas", formatMoney(data.incomeTotal()), INCOME_COLOR, true, false, false, true);
        drawMetricCard(stream, startX + 2 * (cardWidth + gap), row1Top, cardWidth, cardHeight,
            "Despesas", formatMoney(data.expenseTotal()), EXPENSE_COLOR, false, true, false, true);

        // Second row - below first row with gap - normal borders
        float row2Top = row1Top - cardHeight - 12;  // Reduced gap

        // Card 4: Resultado Financeiro
        Color resultColor = data.financialResult().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;
        drawMetricCard(stream, startX, row2Top, cardWidth, cardHeight,
            "Resultado Financeiro", formatMoney(data.financialResult()), resultColor, true, false, false, false);

        // Card 5: Variação do Saldo
        Color changeColor = data.balanceChange().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;
        drawMetricCard(stream, startX + cardWidth + gap, row2Top, cardWidth, cardHeight,
            "Variação do Saldo", formatMoney(data.balanceChange()), changeColor, true, false, false, false);

        // Card 6: Saldo Final (highlighted)
        drawMetricCard(stream, startX + 2 * (cardWidth + gap), row2Top, cardWidth, cardHeight,
            "Saldo Final", formatMoney(data.closingBalance()), TEXT_PRIMARY, false, false, true, false);

        // Return bottom of second row + gap
        return row2Top - cardHeight - 12;
    }

    private float drawMetricCard(PDPageContentStream stream, float x, float y, float width, float height,
                                 String label, String value, Color valueColor, boolean positive, boolean negative, boolean highlight, boolean skipTopBorder) throws IOException {

        // Card background
        Color bgColor = highlight ? PRIMARY_50 : CARD_BG;
        Color borderColor = highlight ? PRIMARY : CARD_BORDER;
        if (skipTopBorder) {
            drawRoundedRectNoTopBorder(stream, x, y - height, width, height, 6, bgColor, borderColor);
        } else {
            drawRoundedRect(stream, x, y - height, width, height, 6, bgColor, borderColor);
        }

        // Label
        drawText(stream, label, x + 8, y - 14, 8, fontRegular, TEXT_MUTED);  // Smaller font, less padding

        // Value
        float valueSize = highlight ? 13 : 11;  // Smaller fonts
        float valueY = y - 32;
        if (highlight) valueY = y - 34;
        drawText(stream, value, x + 8, valueY, valueSize, fontBold, valueColor);

        // Subtitle for first row - only if space permits
        if (height > 50) {
            if (label.equals("Receitas")) {
                drawText(stream, "Entradas no período", x + 8, y - 44, 7, fontRegular, TEXT_MUTED);
            } else if (label.equals("Despesas")) {
                drawText(stream, "Saídas no período", x + 8, y - 44, 7, fontRegular, TEXT_MUTED);
            } else if (label.equals("Resultado Financeiro")) {
                drawText(stream, "Receitas menos despesas", x + 8, y - 44, 7, fontRegular, TEXT_MUTED);
            } else if (label.equals("Variação do Saldo")) {
                drawText(stream, "Inclui transferências de fronteira", x + 8, y - 44, 7, fontRegular, TEXT_MUTED);
            } else if (label.equals("Saldo Final")) {
                drawText(stream, "Saldo inicial + variação", x + 8, y - 44, 7, fontRegular, TEXT_MUTED);
            }
        }

        return y;
    }

    private float drawTransfersSection(PDPageContentStream stream, float y, FinancialReportData data) throws IOException {
        // Section title
        drawText(stream, "Transferências", MARGIN, y, 12, fontBold, TEXT_PRIMARY);  // Smaller
        drawText(stream, "Transferências não são receitas nem despesas.", MARGIN, y - 16, 8, fontRegular, TEXT_MUTED);  // Smaller
        y -= 30;  // Reduced

        float cardHeight = 44f;  // Reduced from 56f
        float cardWidth = (CONTENT_WIDTH - 32f) / 3f;
        float gap = 16f;  // Reduced from 24f
        float startX = MARGIN;

        // All transfer cards on same row
        float rowTop = y;
        drawTransferCard(stream, startX, rowTop, cardWidth, cardHeight,
            "Internas", formatMoney(data.internalTransferTotal()), TRANSFER_COLOR);
        drawTransferCard(stream, startX + cardWidth + gap, rowTop, cardWidth, cardHeight,
            "Recebidas", formatMoney(data.incomingTransferTotal()), INCOME_COLOR);
        drawTransferCard(stream, startX + 2 * (cardWidth + gap), rowTop, cardWidth, cardHeight,
            "Enviadas", formatMoney(data.outgoingTransferTotal()), EXPENSE_COLOR);

        // Return bottom of row + gap
        return rowTop - cardHeight - 12;  // Reduced
    }

    private void drawTransferCard(PDPageContentStream stream, float x, float y, float width, float height,
                                   String label, String value, Color valueColor) throws IOException {
        drawRoundedRect(stream, x, y - height, width, height, 6, CARD_BG, CARD_BORDER);  // Reduced radius

        drawText(stream, label, x + 8, y - 12, 8, fontRegular, TEXT_SECONDARY);  // Smaller
        drawText(stream, value, x + 8, y - 28, 11, fontBold, valueColor);  // Smaller
    }

    private float drawDetailsTable(PDPageContentStream stream, float y, FinancialReportData data) throws IOException {
        // Section title
        drawText(stream, "Movimentações do Período", MARGIN, y, 14, fontBold, TEXT_PRIMARY);
        y -= 24;

        if (data.details() == null || data.details().isEmpty()) {
            drawText(stream, "Nenhuma movimentação no período.", MARGIN + 16, y, 10, fontRegular, TEXT_MUTED);
            drawText(stream, "Os saldos continuam disponíveis no resumo.", MARGIN + 16, y - 16, 9, fontRegular, TEXT_MUTED);
            return y - 40;
        }

        // Table configuration
        String[] columns = {"Data", "Descrição", "Tipo", "Conta Origem", "Conta Destino", "Categoria", "Valor"};
        float[] colWidths = {65f, 120f, 55f, 75f, 75f, 75f, 70f};
        float rowHeight = 20f;
        float tableX = MARGIN;
        float tableWidth = CONTENT_WIDTH;

        // Adjust column widths to fit
        float totalColWidth = 0;
        for (float w : colWidths) totalColWidth += w;
        if (totalColWidth > tableWidth) {
            float scale = tableWidth / totalColWidth;
            for (int i = 0; i < colWidths.length; i++) {
                colWidths[i] *= scale;
            }
        }

        // Table header
        float headerY = y;
        drawRect(stream, tableX, headerY - rowHeight, tableWidth, rowHeight, TABLE_HEADER_BG);
        drawLine(stream, tableX, headerY - rowHeight, tableX + tableWidth, headerY - rowHeight, TABLE_BORDER, 0.5f);

        float colX = tableX;
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                drawLine(stream, colX, headerY, colX, headerY - rowHeight, TABLE_BORDER, 0.5f);
            }
            drawText(stream, columns[i], colX + 5, headerY - 13, 7.5f, fontBold, TEXT_SECONDARY);
            colX += colWidths[i];
        }
        drawLine(stream, colX, headerY, colX, headerY - rowHeight, TABLE_BORDER, 0.5f);

        y = headerY - rowHeight;

        // Table rows
        for (int rowIndex = 0; rowIndex < data.details().size(); rowIndex++) {
            DetailRow row = data.details().get(rowIndex);

            // Check if we need a new page
            if (y - rowHeight < MARGIN + FOOTER_HEIGHT) {
                // We can't easily add new pages in this simplified version, so break
                break;
            }

            // Alternate row background
            if (rowIndex % 2 == 1) {
                drawRect(stream, tableX, y - rowHeight, tableWidth, rowHeight, TABLE_ALT_ROW);
            }

            // Row bottom border
            drawLine(stream, tableX, y - rowHeight, tableX + tableWidth, y - rowHeight, TABLE_BORDER, 0.5f);

            colX = tableX;
            String[] values = {
                formatDate(row.occurredAt()),
                truncate(row.description(), 40),
                row.typeLabel(),
                truncate(row.sourceAccountName(), 20),
                truncate(row.destinationAccountName(), 20),
                truncate(row.categoryName(), 22),
                formatMoney(row.amount())
            };

            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    drawLine(stream, colX, y, colX, y - rowHeight, TABLE_BORDER, 0.5f);
                }

                Color textColor = TEXT_PRIMARY;
                PDFont font = fontRegular;
                float fontSize = 6.5f;
                if (i == 6) { // Valor column
                    font = fontMono;
                    if (row.type().equals("EXPENSE")) textColor = EXPENSE_COLOR;
                    else if (row.type().equals("INCOME")) textColor = INCOME_COLOR;
                    else textColor = TRANSFER_COLOR;
                } else if (i == 2) { // Tipo column
                    font = fontRegular;
                    if (row.type().equals("EXPENSE")) textColor = EXPENSE_COLOR;
                    else if (row.type().equals("INCOME")) textColor = INCOME_COLOR;
                    else textColor = TRANSFER_COLOR;
                }

                drawText(stream, values[i], colX + 5, y - 13, fontSize, font, textColor);
                colX += colWidths[i];
            }
            drawLine(stream, colX, y, colX, y - rowHeight, TABLE_BORDER, 0.5f);

            y -= rowHeight;
        }

        // Table outer border
        drawRectOutline(stream, tableX, headerY - rowHeight, tableWidth, headerY - y, TABLE_BORDER, 0.5f);

        return y - 16;
    }

    private void drawFooter(PDPageContentStream stream) throws IOException {
        float y = MARGIN;
        drawLine(stream, MARGIN, y + 10, PAGE_WIDTH - MARGIN, y + 10, DIVIDER, 0.5f);
        drawText(stream, "Equilibra - Relatório Financeiro", MARGIN, y - 4, 8, fontRegular, TEXT_MUTED);
        String pageText = "Página 1 de 1";
        float pageWidth = fontRegular.getStringWidth(pageText) / 1000f * 8;
        drawText(stream, pageText, PAGE_WIDTH - MARGIN - pageWidth, y - 4, 8, fontRegular, TEXT_MUTED);
    }

    // Drawing primitives

    private void drawRect(PDPageContentStream stream, float x, float y, float width, float height, Color color) throws IOException {
        stream.setNonStrokingColor(toPDColor(color));
        stream.addRect(x, y, width, height);
        stream.fill();
    }

    private void drawRectOutline(PDPageContentStream stream, float x, float y, float width, float height, Color color, float lineWidth) throws IOException {
        stream.setStrokingColor(toPDColor(color));
        stream.setLineWidth(lineWidth);
        stream.addRect(x, y, width, height);
        stream.stroke();
    }

    private void drawRoundedRect(PDPageContentStream stream, float x, float y, float width, float height, float radius,
                                 Color fillColor, Color borderColor) throws IOException {
        // Fill
        stream.setNonStrokingColor(toPDColor(fillColor));
        drawRoundedRectPath(stream, x, y, width, height, radius);
        stream.fill();

        // Border
        stream.setStrokingColor(toPDColor(borderColor));
        stream.setLineWidth(0.5f);
        drawRoundedRectPath(stream, x, y, width, height, radius);
        stream.stroke();
    }

    private void drawRoundedRectNoTopBorder(PDPageContentStream stream, float x, float y, float width, float height, float radius,
                                             Color fillColor, Color borderColor) throws IOException {
        // Fill (same as regular)
        stream.setNonStrokingColor(toPDColor(fillColor));
        drawRoundedRectPath(stream, x, y, width, height, radius);
        stream.fill();

        // Border without top edge
        stream.setStrokingColor(toPDColor(borderColor));
        stream.setLineWidth(0.5f);
        drawRoundedRectPathNoTop(stream, x, y, width, height, radius);
        stream.stroke();
    }

    private void drawRoundedRectPathNoTop(PDPageContentStream stream, float x, float y, float width, float height, float radius) throws IOException {
        // Start from top-right corner, go clockwise but skip top edge
        stream.moveTo(x + width, y);
        stream.lineTo(x + width, y + height - radius);
        stream.curveTo(x + width, y + height, x + width, y + height, x + width - radius, y + height);
        stream.lineTo(x + radius, y + height);
        stream.curveTo(x, y + height, x, y + height, x, y + height - radius);
        stream.lineTo(x, y + radius);
        stream.curveTo(x, y, x, y, x + radius, y);
        // Don't close path - this leaves the top open
    }

    private void drawRoundedRectPath(PDPageContentStream stream, float x, float y, float width, float height, float radius) throws IOException {
        stream.moveTo(x + radius, y);
        stream.lineTo(x + width - radius, y);
        stream.curveTo(x + width, y, x + width, y, x + width, y + radius);
        stream.lineTo(x + width, y + height - radius);
        stream.curveTo(x + width, y + height, x + width, y + height, x + width - radius, y + height);
        stream.lineTo(x + radius, y + height);
        stream.curveTo(x, y + height, x, y + height, x, y + height - radius);
        stream.lineTo(x, y + radius);
        stream.curveTo(x, y, x, y, x + radius, y);
        stream.closePath();
    }

    private void drawLine(PDPageContentStream stream, float x1, float y1, float x2, float y2, Color color, float lineWidth) throws IOException {
        stream.setStrokingColor(toPDColor(color));
        stream.setLineWidth(lineWidth);
        stream.moveTo(x1, y1);
        stream.lineTo(x2, y2);
        stream.stroke();
    }

    private void drawText(PDPageContentStream stream, String text, float x, float y, float fontSize, PDFont font, Color color) throws IOException {
        if (text == null) return;
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.setNonStrokingColor(toPDColor(color));
        stream.newLineAtOffset(x, y);
        stream.showText(safe(text));
        stream.endText();
    }

    private PDColor toPDColor(Color color) {
        return new PDColor(new float[]{
            color.getRed() / 255f,
            color.getGreen() / 255f,
            color.getBlue() / 255f
        }, PDDeviceRGB.INSTANCE);
    }

    private String safe(String value) {
        return value == null ? "" : value.replaceAll("[^\\x20-\\x7E\\u00C0-\\u024F]", "?");
    }

    private String cell(String raw) {
        String value = raw == null ? "" : raw.replace("\r", " ").replace("\n", " ");
        if (value.matches("^[=+\\-@\\t].*")) value = "'" + value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    public static String money(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value == null ? BigDecimal.ZERO : value);
    }

    public static String date(Instant value) {
        return value == null ? "" : value.toString();
    }

    public static String date(String value) {
        return value == null ? "" : value;
    }

    private String formatMoney(BigDecimal value) {
        return "R$ " + money(value);
    }

    private String formatDate(String isoString) {
        if (isoString == null) return "";
        try {
            Instant instant = Instant.parse(isoString);
            return DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Sao_Paulo")).format(instant);
        } catch (Exception e) {
            return isoString;
        }
    }

    private String formatDateTime(Instant instant) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo")).format(instant);
    }

    private String formatPeriod(Instant from, Instant to) {
        String fromStr = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Sao_Paulo")).format(from);
        String toStr = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("America/Sao_Paulo")).format(to);
        return "Período: " + fromStr + " a " + toStr;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 1) + "…";
    }

    // Data records for structured PDF generation
    public record FinancialReportData(
        Instant periodFrom,
        Instant periodTo,
        List<String> accounts,
        BigDecimal openingBalance,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        BigDecimal financialResult,
        BigDecimal incomingTransferTotal,
        BigDecimal outgoingTransferTotal,
        BigDecimal internalTransferTotal,
        BigDecimal balanceChange,
        BigDecimal closingBalance,
        List<DetailRow> details
    ) {}

    public record CategoryReportData(
        Instant periodFrom,
        Instant periodTo,
        List<String> accounts,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        BigDecimal netResult,
        List<CategoryRow> categories
    ) {}

    public record CategoryRow(
        String categoryName,
        String applicability,
        BigDecimal incomeTotal,
        BigDecimal expenseTotal,
        BigDecimal netResult
    ) {}

    public record DetailRow(
        String id,
        String occurredAt,
        String description,
        String type,
        String typeLabel,
        BigDecimal amount,
        String categoryName,
        String sourceAccountName,
        String destinationAccountName
    ) {}
}