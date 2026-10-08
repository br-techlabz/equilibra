package br.com.equilibra.report.category.application;

import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.report.category.api.CategoryReportResponse;
import br.com.equilibra.report.category.infrastructure.CategoryReportRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.domain.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryReportQueryService {
    private final AssetAccountRepository accounts;
    private final CategoryRepository categories;
    private final CategoryReportRepository repository;
    private final CurrentUser currentUser;
    public CategoryReportQueryService(AssetAccountRepository accounts, CategoryRepository categories, CategoryReportRepository repository, CurrentUser currentUser){this.accounts=accounts;this.categories=categories;this.repository=repository;this.currentUser=currentUser;}

    @Transactional(readOnly=true)
    public CategoryReportResponse query(Instant from, Instant to, List<String> accountIds){
        if(from==null||to==null||!from.isBefore(to))throw new IllegalArgumentException("from must be before to");
        String owner=currentUser.id().toString(); List<String> ids=accountIds==null?List.of():accountIds.stream().filter(Objects::nonNull).distinct().toList();
        List<AssetAccount> selected=ids.isEmpty()?accounts.findAllByOwnerIdOrderByNameAsc(owner):ids.stream().map(id->accounts.findByIdAndOwnerId(id,owner).orElseThrow(()->new ResourceNotFoundException("Asset account not found."))).toList();
        List<String> selectedIds=selected.stream().map(AssetAccount::getId).toList();
        Map<String,Category> categoryById=categories.findAllByOwnerIdOrderByNameAsc(owner).stream().collect(Collectors.toMap(Category::getId, c->c));
        BigDecimal income=BigDecimal.ZERO,expense=BigDecimal.ZERO; Map<String,BigDecimal[]> grouped=new HashMap<>();
        for(CategoryReportRepository.CategoryAggregate row:repository.aggregate(owner,TransactionStatus.ACTIVE,from,to,selectedIds)){BigDecimal in=row.getIncomeTotal()==null?BigDecimal.ZERO:row.getIncomeTotal();BigDecimal ex=row.getExpenseTotal()==null?BigDecimal.ZERO:row.getExpenseTotal();grouped.put(row.getCategoryId(),new BigDecimal[]{in,ex});income=income.add(in);expense=expense.add(ex);}
        List<CategoryReportResponse.CategoryGroup> groups=grouped.entrySet().stream().map(e->{Category c=categoryById.get(e.getKey());if(c==null)return null;BigDecimal in=e.getValue()[0],ex=e.getValue()[1];return new CategoryReportResponse.CategoryGroup(c.getId(),c.getName(),c.getApplicability(),in,ex,in.subtract(ex));}).filter(Objects::nonNull).sorted(Comparator.comparing(CategoryReportResponse.CategoryGroup::categoryName)).toList();
        return new CategoryReportResponse(income,expense,income.subtract(expense),groups);
    }
}
