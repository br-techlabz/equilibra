package br.com.equilibra.budget.infrastructure;
import br.com.equilibra.budget.domain.CategoryBudget; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget,String>{Optional<CategoryBudget> findByIdAndOwnerId(String id,String ownerId); List<CategoryBudget> findAllByOwnerIdOrderByYearDescMonthDesc(String ownerId); Optional<CategoryBudget> findByOwnerIdAndCategoryIdAndYearAndMonth(String ownerId,String categoryId,short year,byte month);}
