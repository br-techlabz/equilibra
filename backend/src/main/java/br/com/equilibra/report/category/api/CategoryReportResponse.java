package br.com.equilibra.report.category.api;

import br.com.equilibra.category.domain.CategoryApplicability;
import java.math.BigDecimal;
import java.util.List;

public record CategoryReportResponse(
    BigDecimal incomeTotal,
    BigDecimal expenseTotal,
    BigDecimal netResult,
    List<CategoryGroup> categories
) {
    public record CategoryGroup(String categoryId, String categoryName, CategoryApplicability applicability,
                                 BigDecimal incomeTotal, BigDecimal expenseTotal, BigDecimal netResult) {}
}
