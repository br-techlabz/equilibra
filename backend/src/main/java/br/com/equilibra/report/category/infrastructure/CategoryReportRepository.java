package br.com.equilibra.report.category.infrastructure;

import br.com.equilibra.category.domain.CategoryApplicability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TransactionStatus;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface CategoryReportRepository extends JpaRepository<FinancialTransaction, String> {
    interface CategoryAggregate {
        String getCategoryId();
        BigDecimal getIncomeTotal();
        BigDecimal getExpenseTotal();
    }

    @Query("select t.categoryId as categoryId, sum(case when t.type = br.com.equilibra.transaction.domain.TransactionType.INCOME then t.amount else 0 end) as incomeTotal, sum(case when t.type = br.com.equilibra.transaction.domain.TransactionType.EXPENSE then t.amount else 0 end) as expenseTotal from FinancialTransaction t where t.ownerId = :ownerId and t.status = :status and t.occurredAt >= :from and t.occurredAt < :to and (t.sourceAccountId in :accountIds or t.destinationAccountId in :accountIds) and t.type in (br.com.equilibra.transaction.domain.TransactionType.INCOME, br.com.equilibra.transaction.domain.TransactionType.EXPENSE) group by t.categoryId")
    List<CategoryAggregate> aggregate(@Param("ownerId") String ownerId, @Param("status") TransactionStatus status, @Param("from") Instant from, @Param("to") Instant to, @Param("accountIds") Collection<String> accountIds);
}
