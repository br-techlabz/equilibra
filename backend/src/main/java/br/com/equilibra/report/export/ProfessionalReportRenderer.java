package br.com.equilibra.report.export;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class ProfessionalReportRenderer {

    // Equilibra Design System Colors
    private static final Color PRIMARY_DARK = new DeviceRgb(0x4C, 0x1D, 0x95); // #4c1d95
    private static final Color PRIMARY = new DeviceRgb(0x7C, 0x3A, 0xED);      // #7c3aed
    private static final Color PRIMARY_LIGHT = new DeviceRgb(0xED, 0xE9, 0xFE); // #ede9fe
    private static final Color PRIMARY_50 = new DeviceRgb(0xF5, 0xF3, 0xFF);    // #f5f3ff
    private static final Color TEXT_PRIMARY = new DeviceRgb(0x0F, 0x17, 0x2A);  // #0f172a
    private static final Color TEXT_SECONDARY = new DeviceRgb(0x33, 0x41, 0x55); // #334155
    private static final Color TEXT_MUTED = new DeviceRgb(0x64, 0x74, 0x8B);    // #64748b
    private static final Color TEXT_ON_PRIMARY = ColorConstants.WHITE;
    private static final Color CARD_BG = ColorConstants.WHITE;
    private static final Color CARD_BORDER = new DeviceRgb(0xE2, 0xE8, 0xF0);   // #e2e8f0
    private static final Color INCOME_COLOR = new DeviceRgb(0x5B, 0x21, 0xB6);  // #5b21b6
    private static final Color INCOME_BG = new DeviceRgb(0xF5, 0xF3, 0xFF);     // #f5f3ff
    private static final Color EXPENSE_COLOR = new DeviceRgb(0xB8, 0x2E, 0x2E); // #b82e2e
    private static final Color EXPENSE_BG = new DeviceRgb(0xFD, 0xF2, 0xF2);    // #fdf2f2
    private static final Color TRANSFER_COLOR = new DeviceRgb(0x0D, 0x47, 0xA1); // #0d47a1
    private static final Color TABLE_HEADER_BG = new DeviceRgb(0xF8, 0xFA, 0xFC); // #f8fafc
    private static final Color TABLE_BORDER = new DeviceRgb(0xE2, 0xE8, 0xF0);  // #e2e8f0
    private static final Color TABLE_ALT_ROW = new DeviceRgb(0xFA, 0xFA, 0xFC);  // #fafafc
    private static final Color SUCCESS_GREEN = new DeviceRgb(0x38, 0x8E, 0x3C);  // #388e3c
    private static final Color DANGER_RED = new DeviceRgb(0xB8, 0x2E, 0x2E);    // #b82e2e

    private PdfFont fontRegular;
    private PdfFont fontBold;
    private PdfFont fontMono;

    public byte[] pdfFinancialReport(FinancialReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(output);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(48, 48, 48, 48);
            initFonts(pdf);

            // Header
            addFinancialHeader(document, data);

            // Summary Cards (2 rows x 3)
            addFinancialSummaryCards(document, data);

            // Transfers Section
            addTransfersSection(document, data);

            // Details Table
            addDetailsTable(document, data);

            // Footer
            addFooter(document);

            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate financial report PDF", e);
        }
    }

    public byte[] pdfCategoryReport(CategoryReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(output);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(48, 48, 48, 48);
            initFonts(pdf);

            // Header
            addCategoryHeader(document, data);

            // Summary Cards (3 cards)
            addCategorySummaryCards(document, data);

            // Categories Table
            addCategoryTable(document, data);

            // Footer
            addFooter(document);

            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate category report PDF", e);
        }
    }

    public byte[] pdfAuditReport(AuditReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(output);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(48, 48, 48, 48);
            initFonts(pdf);

            // Header
            addAuditHeader(document, data);

            // Summary
            addAuditSummary(document, data);

            // Audit Table
            addAuditTable(document, data);

            // Footer
            addFooter(document);

            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate audit report PDF", e);
        }
    }

    private void initFonts(PdfDocument pdf) throws Exception {
        fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        fontMono = PdfFontFactory.createFont(StandardFonts.COURIER);
    }

    // ========== Financial Report Methods ==========

    private void addFinancialHeader(Document document, FinancialReportData data) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
            .useAllAvailableWidth();

        // Left side - Title & Info
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        Paragraph title = new Paragraph("Relatório Financeiro")
            .setFont(fontBold).setFontSize(22).setFontColor(TEXT_ON_PRIMARY);
        Paragraph subtitle = new Paragraph("Equilibra - Gerenciamento de Finanças Domésticas")
            .setFont(fontRegular).setFontSize(10).setFontColor(new DeviceRgb(0xC4, 0xB5, 0xFD));
        Paragraph period = new Paragraph(formatPeriod(data.periodFrom(), data.periodTo()))
            .setFont(fontRegular).setFontSize(9).setFontColor(TEXT_ON_PRIMARY);
        Paragraph generated = new Paragraph("Gerado em " + formatDateTime(Instant.now()))
            .setFont(fontRegular).setFontSize(8).setFontColor(new DeviceRgb(0xA7, 0x8B, 0xFA));

        leftCell.add(title).add(subtitle).add(period).add(generated);

        if (data.accounts() != null && !data.accounts().isEmpty()) {
            Paragraph accounts = new Paragraph("Contas: " + String.join(", ", data.accounts()))
                .setFont(fontRegular).setFontSize(8).setFontColor(new DeviceRgb(0xA7, 0x8B, 0xFA));
            leftCell.add(accounts);
        }

        // Right side - empty for alignment
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
            .setTextAlignment(TextAlignment.RIGHT);

        headerTable.addCell(leftCell).addCell(rightCell);

        // Wrap in a container with background
        Div headerDiv = new Div()
            .setBackgroundColor(PRIMARY_DARK)
            .setPadding(16)
            .setBorderRadius(new BorderRadius(8))
            .add(headerTable);

        document.add(headerDiv);
        document.add(new Paragraph("\n")); // spacing
    }

    private void addFinancialSummaryCards(Document document, FinancialReportData data) {
        // Row 1
        Table row1 = createSummaryRow(
            new SummaryCard("Saldo Inicial", formatMoney(data.openingBalance()), TEXT_PRIMARY, false),
            new SummaryCard("Receitas", formatMoney(data.incomeTotal()), INCOME_COLOR, true),
            new SummaryCard("Despesas", formatMoney(data.expenseTotal()), EXPENSE_COLOR, false)
        );
        document.add(row1);
        document.add(new Paragraph("\n").setMarginBottom(8));

        // Row 2
        Color resultColor = data.financialResult().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;
        Color changeColor = data.balanceChange().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;

        Table row2 = createSummaryRow(
            new SummaryCard("Resultado Financeiro", formatMoney(data.financialResult()), resultColor, true, "Receitas menos despesas"),
            new SummaryCard("Variação do Saldo", formatMoney(data.balanceChange()), changeColor, true, "Inclui transferências de fronteira"),
            new SummaryCard("Saldo Final", formatMoney(data.closingBalance()), TEXT_PRIMARY, true, "Saldo inicial + variação", true)
        );
        document.add(row2);
        document.add(new Paragraph("\n").setMarginBottom(8));
    }

    private Table createSummaryRow(SummaryCard... cards) {
        float[] widths = new float[cards.length];
        for (int i = 0; i < cards.length; i++) widths[i] = 100f / cards.length;

        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

        for (SummaryCard card : cards) {
            Cell cell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
            Div cardDiv = createMetricCard(card);
            cell.add(cardDiv);
            table.addCell(cell);
        }
        return table;
    }

    private Div createMetricCard(SummaryCard card) {
        Color bgColor = card.highlight ? PRIMARY_50 : CARD_BG;
        Color borderColor = card.highlight ? PRIMARY : CARD_BORDER;

        Div cardDiv = new Div()
            .setBackgroundColor(bgColor)
            .setBorder(new SolidBorder(borderColor, 0.5f))
            .setBorderRadius(new BorderRadius(6))
            .setPadding(12);

        Paragraph label = new Paragraph(card.label)
            .setFont(fontRegular).setFontSize(8).setFontColor(TEXT_MUTED).setMarginBottom(4);

        Paragraph value = new Paragraph(card.value)
            .setFont(fontBold).setFontSize(card.highlight ? 13 : 11).setFontColor(card.color).setMarginBottom(2);

        cardDiv.add(label).add(value);

        if (card.subtitle != null) {
            Paragraph subtitle = new Paragraph(card.subtitle)
                .setFont(fontRegular).setFontSize(7).setFontColor(TEXT_MUTED);
            cardDiv.add(subtitle);
        }

        return cardDiv;
    }

    private record SummaryCard(
        String label,
        String value,
        Color color,
        boolean positive,
        String subtitle,
        boolean highlight
    ) {
        public SummaryCard(String label, String value, Color color, boolean positive) {
            this(label, value, color, positive, null, false);
        }
        public SummaryCard(String label, String value, Color color, boolean positive, String subtitle) {
            this(label, value, color, positive, subtitle, false);
        }
        public SummaryCard(String label, String value, Color color, boolean positive, String subtitle, boolean highlight) {
            this.label = label;
            this.value = value;
            this.color = color;
            this.positive = positive;
            this.subtitle = subtitle;
            this.highlight = highlight;
        }
    }

    private void addTransfersSection(Document document, FinancialReportData data) {
        // Section title
        Paragraph title = new Paragraph("Transferências")
            .setFont(fontBold).setFontSize(12).setFontColor(TEXT_PRIMARY).setMarginBottom(4);
        Paragraph subtitle = new Paragraph("Transferências não são receitas nem despesas.")
            .setFont(fontRegular).setFontSize(8).setFontColor(TEXT_MUTED).setMarginBottom(12);
        document.add(title);
        document.add(subtitle);

        Table table = createSummaryRow(
            new SummaryCard("Internas", formatMoney(data.internalTransferTotal()), TRANSFER_COLOR, false),
            new SummaryCard("Recebidas", formatMoney(data.incomingTransferTotal()), INCOME_COLOR, true),
            new SummaryCard("Enviadas", formatMoney(data.outgoingTransferTotal()), EXPENSE_COLOR, false)
        );
        document.add(table);
        document.add(new Paragraph("\n").setMarginBottom(8));
    }

    private void addDetailsTable(Document document, FinancialReportData data) {
        Paragraph title = new Paragraph("Movimentações do Período")
            .setFont(fontBold).setFontSize(12).setFontColor(TEXT_PRIMARY).setMarginBottom(8);
        document.add(title);

        if (data.details() == null || data.details().isEmpty()) {
            Paragraph empty = new Paragraph("Nenhuma movimentação no período. Os saldos continuam disponíveis no resumo.")
                .setFont(fontRegular).setFontSize(10).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.CENTER).setMarginTop(20);
            document.add(empty);
            return;
        }

        String[] columns = {"Data", "Descrição", "Tipo", "Conta Origem", "Conta Destino", "Categoria", "Valor"};
        float[] widths = {10, 20, 9, 13, 13, 13, 12};

        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

        // Header
        for (String col : columns) {
            Cell cell = new Cell()
                .setBackgroundColor(TABLE_HEADER_BG)
                .setBorder(new SolidBorder(TABLE_BORDER, 0.5f))
                .setPadding(6)
                .add(new Paragraph(col).setFont(fontBold).setFontSize(7).setFontColor(TEXT_SECONDARY));
            table.addHeaderCell(cell);
        }

        // Rows
        for (int i = 0; i < data.details().size(); i++) {
            DetailRow row = data.details().get(i);
            Color rowBg = (i % 2 == 1) ? TABLE_ALT_ROW : ColorConstants.WHITE;

            String[] values = {
                formatDate(row.occurredAt()),
                truncate(row.description(), 40),
                row.typeLabel(),
                truncate(row.sourceAccountName(), 18),
                truncate(row.destinationAccountName(), 18),
                truncate(row.categoryName(), 20),
                formatMoney(row.amount())
            };

            for (int j = 0; j < values.length; j++) {
                Color textColor = TEXT_PRIMARY;
                PdfFont font = fontRegular;
                float fontSize = 6.5f;

                if (j == 6) { // Valor
                    font = fontMono;
                    if (row.type().equals("EXPENSE")) textColor = EXPENSE_COLOR;
                    else if (row.type().equals("INCOME")) textColor = INCOME_COLOR;
                    else textColor = TRANSFER_COLOR;
                } else if (j == 2) { // Tipo
                    if (row.type().equals("EXPENSE")) textColor = EXPENSE_COLOR;
                    else if (row.type().equals("INCOME")) textColor = INCOME_COLOR;
                    else textColor = TRANSFER_COLOR;
                }

                Cell cell = new Cell()
                    .setBackgroundColor(rowBg)
                    .setBorder(new SolidBorder(TABLE_BORDER, 0.5f))
                    .setPadding(5)
                    .add(new Paragraph(values[j]).setFont(font).setFontSize(fontSize).setFontColor(textColor));
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    // ========== Category Report Methods ==========

    private void addCategoryHeader(Document document, CategoryReportData data) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
            .useAllAvailableWidth();

        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        Paragraph title = new Paragraph("Relatório por Categoria")
            .setFont(fontBold).setFontSize(22).setFontColor(TEXT_ON_PRIMARY);
        Paragraph subtitle = new Paragraph("Equilibra - Distribuição de Receitas e Despesas")
            .setFont(fontRegular).setFontSize(10).setFontColor(new DeviceRgb(0xC4, 0xB5, 0xFD));
        Paragraph period = new Paragraph(formatPeriod(data.periodFrom(), data.periodTo()))
            .setFont(fontRegular).setFontSize(9).setFontColor(TEXT_ON_PRIMARY);
        Paragraph generated = new Paragraph("Gerado em " + formatDateTime(Instant.now()))
            .setFont(fontRegular).setFontSize(8).setFontColor(new DeviceRgb(0xA7, 0x8B, 0xFA));

        leftCell.add(title).add(subtitle).add(period).add(generated);

        if (data.accounts() != null && !data.accounts().isEmpty()) {
            Paragraph accounts = new Paragraph("Contas: " + String.join(", ", data.accounts()))
                .setFont(fontRegular).setFontSize(8).setFontColor(new DeviceRgb(0xA7, 0x8B, 0xFA));
            leftCell.add(accounts);
        }

        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
            .setTextAlignment(TextAlignment.RIGHT);

        headerTable.addCell(leftCell).addCell(rightCell);

        Div headerDiv = new Div()
            .setBackgroundColor(PRIMARY_DARK)
            .setPadding(16)
            .setBorderRadius(new BorderRadius(8))
            .add(headerTable);

        document.add(headerDiv);
        document.add(new Paragraph("\n"));
    }

    private void addCategorySummaryCards(Document document, CategoryReportData data) {
        Color resultColor = data.netResult().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;
        Table table = createSummaryRow(
            new SummaryCard("Total Receitas", formatMoney(data.incomeTotal()), INCOME_COLOR, true),
            new SummaryCard("Total Despesas", formatMoney(data.expenseTotal()), EXPENSE_COLOR, false),
            new SummaryCard("Resultado", formatMoney(data.netResult()), resultColor, true)
        );
        document.add(table);
        document.add(new Paragraph("\n").setMarginBottom(8));
    }

    private void addCategoryTable(Document document, CategoryReportData data) {
        Paragraph title = new Paragraph("Categorias")
            .setFont(fontBold).setFontSize(12).setFontColor(TEXT_PRIMARY).setMarginBottom(4);
        Paragraph subtitle = new Paragraph("Receitas e despesas separadas por categoria.")
            .setFont(fontRegular).setFontSize(8).setFontColor(TEXT_MUTED).setMarginBottom(12);
        document.add(title);
        document.add(subtitle);

        if (data.categories() == null || data.categories().isEmpty()) {
            Paragraph empty = new Paragraph("Nenhuma categoria no período. Não há receitas ou despesas para os filtros selecionados.")
                .setFont(fontRegular).setFontSize(10).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.CENTER).setMarginTop(20);
            document.add(empty);
            return;
        }

        String[] columns = {"Categoria", "Aplicabilidade", "Receitas", "Despesas", "Resultado"};
        float[] widths = {30, 18, 18, 18, 16};

        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

        for (String col : columns) {
            Cell cell = new Cell()
                .setBackgroundColor(TABLE_HEADER_BG)
                .setBorder(new SolidBorder(TABLE_BORDER, 0.5f))
                .setPadding(6)
                .add(new Paragraph(col).setFont(fontBold).setFontSize(7).setFontColor(TEXT_SECONDARY));
            table.addHeaderCell(cell);
        }

        for (int i = 0; i < data.categories().size(); i++) {
            CategoryRow row = data.categories().get(i);
            Color rowBg = (i % 2 == 1) ? TABLE_ALT_ROW : ColorConstants.WHITE;

            String[] values = {
                truncate(row.categoryName(), 35),
                row.applicability(),
                formatMoney(row.incomeTotal()),
                formatMoney(row.expenseTotal()),
                formatMoney(row.netResult())
            };

            for (int j = 0; j < values.length; j++) {
                Color textColor = TEXT_PRIMARY;
                PdfFont font = fontRegular;
                float fontSize = 6.5f;

                if (j == 2) { font = fontMono; textColor = INCOME_COLOR; }
                else if (j == 3) { font = fontMono; textColor = EXPENSE_COLOR; }
                else if (j == 4) {
                    font = fontMono;
                    textColor = row.netResult().compareTo(BigDecimal.ZERO) >= 0 ? INCOME_COLOR : EXPENSE_COLOR;
                }

                Cell cell = new Cell()
                    .setBackgroundColor(rowBg)
                    .setBorder(new SolidBorder(TABLE_BORDER, 0.5f))
                    .setPadding(5)
                    .add(new Paragraph(values[j]).setFont(font).setFontSize(fontSize).setFontColor(textColor));
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    // ========== Audit Report Methods ==========

    private void addAuditHeader(Document document, AuditReportData data) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
            .useAllAvailableWidth();

        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0);
        Paragraph title = new Paragraph("Relatório de Auditoria")
            .setFont(fontBold).setFontSize(22).setFontColor(TEXT_ON_PRIMARY);
        Paragraph subtitle = new Paragraph("Equilibra - Log de Transações e Operações")
            .setFont(fontRegular).setFontSize(10).setFontColor(new DeviceRgb(0xC4, 0xB5, 0xFD));
        Paragraph period = new Paragraph(formatPeriod(data.periodFrom(), data.periodTo()))
            .setFont(fontRegular).setFontSize(9).setFontColor(TEXT_ON_PRIMARY);
        Paragraph generated = new Paragraph("Gerado em " + formatDateTime(Instant.now()))
            .setFont(fontRegular).setFontSize(8).setFontColor(new DeviceRgb(0xA7, 0x8B, 0xFA));

        leftCell.add(title).add(subtitle).add(period).add(generated);

        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0)
            .setTextAlignment(TextAlignment.RIGHT);

        headerTable.addCell(leftCell).addCell(rightCell);

        Div headerDiv = new Div()
            .setBackgroundColor(PRIMARY_DARK)
            .setPadding(16)
            .setBorderRadius(new BorderRadius(8))
            .add(headerTable);

        document.add(headerDiv);
        document.add(new Paragraph("\n"));
    }

    private void addAuditSummary(Document document, AuditReportData data) {
        Table table = createSummaryRow(
            new SummaryCard("Total Transações", String.valueOf(data.totalCount()), TEXT_PRIMARY, false),
            new SummaryCard("Volume Total", formatMoney(data.totalVolume()), INCOME_COLOR, true),
            new SummaryCard("Período", formatPeriod(data.periodFrom(), data.periodTo()).replace("Período: ", ""), TEXT_PRIMARY, false)
        );
        document.add(table);
        document.add(new Paragraph("\n").setMarginBottom(8));
    }

    private void addAuditTable(Document document, AuditReportData data) {
        Paragraph title = new Paragraph("Transações Auditoria")
            .setFont(fontBold).setFontSize(12).setFontColor(TEXT_PRIMARY).setMarginBottom(8);
        document.add(title);

        if (data.transactions() == null || data.transactions().isEmpty()) {
            Paragraph empty = new Paragraph("Nenhuma transação no período.")
                .setFont(fontRegular).setFontSize(10).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.CENTER).setMarginTop(20);
            document.add(empty);
            return;
        }

        String[] columns = {"Data", "Tipo", "Status", "Descrição", "Valor", "Conta", "Categoria"};
        float[] widths = {12, 10, 10, 22, 12, 14, 20};

        Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

        for (String col : columns) {
            Cell cell = new Cell()
                .setBackgroundColor(TABLE_HEADER_BG)
                .setBorder(new SolidBorder(TABLE_BORDER, 0.5f))
                .setPadding(6)
                .add(new Paragraph(col).setFont(fontBold).setFontSize(7).setFontColor(TEXT_SECONDARY));
            table.addHeaderCell(cell);
        }

        for (int i = 0; i < data.transactions().size(); i++) {
            AuditRow row = data.transactions().get(i);
            Color rowBg = (i % 2 == 1) ? TABLE_ALT_ROW : ColorConstants.WHITE;

            String[] values = {
                formatDate(row.occurredAt()),
                row.type(),
                row.status(),
                truncate(row.description(), 45),
                formatMoney(row.amount()),
                truncate(row.accountName(), 20),
                truncate(row.categoryName(), 25)
            };

            for (int j = 0; j < values.length; j++) {
                Color textColor = TEXT_PRIMARY;
                PdfFont font = fontRegular;
                float fontSize = 6f;

                if (j == 4) font = fontMono;

                Cell cell = new Cell()
                    .setBackgroundColor(rowBg)
                    .setBorder(new SolidBorder(TABLE_BORDER, 0.5f))
                    .setPadding(4)
                    .add(new Paragraph(values[j]).setFont(font).setFontSize(fontSize).setFontColor(textColor));
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("Equilibra - Relatório Financeiro | Página 1 de 1")
            .setFont(fontRegular).setFontSize(8).setFontColor(TEXT_MUTED)
            .setTextAlignment(TextAlignment.CENTER).setMarginTop(20);
        document.add(footer);
    }

    // ========== Utility Methods ==========

    private String formatMoney(BigDecimal value) {
        return "R$ " + money(value);
    }

    public static String money(BigDecimal value) {
        java.text.NumberFormat format = java.text.NumberFormat.getNumberInstance(new Locale("pt", "BR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(value == null ? BigDecimal.ZERO : value);
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

    // ========== Data Records ==========

    public record FinancialReportData(
        Instant periodFrom, Instant periodTo, List<String> accounts,
        BigDecimal openingBalance, BigDecimal incomeTotal, BigDecimal expenseTotal,
        BigDecimal financialResult, BigDecimal incomingTransferTotal,
        BigDecimal outgoingTransferTotal, BigDecimal internalTransferTotal,
        BigDecimal balanceChange, BigDecimal closingBalance,
        List<DetailRow> details
    ) {}

    public record DetailRow(
        String id, String occurredAt, String description, String type, String typeLabel,
        BigDecimal amount, String categoryName, String sourceAccountName, String destinationAccountName
    ) {}

    public record CategoryReportData(
        Instant periodFrom, Instant periodTo, List<String> accounts,
        BigDecimal incomeTotal, BigDecimal expenseTotal, BigDecimal netResult,
        List<CategoryRow> categories
    ) {}

    public record CategoryRow(
        String categoryName, String applicability,
        BigDecimal incomeTotal, BigDecimal expenseTotal, BigDecimal netResult
    ) {}

    public record AuditReportData(
        Instant periodFrom, Instant periodTo, List<String> accounts,
        int totalCount, BigDecimal totalVolume,
        List<AuditRow> transactions
    ) {}

    public record AuditRow(
        String occurredAt, String type, String status, String description,
        BigDecimal amount, String accountName, String categoryName
    ) {}
}