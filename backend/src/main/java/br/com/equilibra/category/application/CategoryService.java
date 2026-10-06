package br.com.equilibra.category.application;

import br.com.equilibra.category.api.CategoryResponse;
import br.com.equilibra.category.api.CreateCategoryRequest;
import br.com.equilibra.category.api.UpdateCategoryRequest;
import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import br.com.equilibra.category.infrastructure.CategoryRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.transaction.domain.TransactionType;
import br.com.equilibra.transaction.infrastructure.FinancialTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final FinancialTransactionRepository transactions;
    private final CurrentUser currentUser;

    public CategoryService(CategoryRepository repository, FinancialTransactionRepository transactions,
                            CurrentUser currentUser) {
        this.repository = repository;
        this.transactions = transactions;
        this.currentUser = currentUser;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        String ownerId = ownerId();
        ensureNameAvailable(ownerId, Category.normalizeName(request.name()), null);
        try {
            return CategoryResponse.from(repository.save(new Category(ownerId, request.name(), request.applicability())));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateName();
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(boolean includeInactive, CategoryApplicability applicability) {
        String ownerId = ownerId();
        List<Category> categories;
        if (applicability == null) {
            categories = includeInactive
                ? repository.findAllByOwnerIdOrderByNameAsc(ownerId)
                : repository.findAllByOwnerIdAndActiveTrueOrderByNameAsc(ownerId);
        } else if (includeInactive) {
            categories = repository.findAllByOwnerIdOrderByNameAsc(ownerId).stream()
                .filter(category -> supports(category, applicability))
                .toList();
        } else {
            categories = repository.findAllByOwnerIdAndActiveTrueAndApplicabilityInOrderByNameAsc(
                ownerId, applicableValues(applicability));
        }
        return categories.stream().map(CategoryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(String id) {
        return CategoryResponse.from(findOwned(id));
    }

    @Transactional
    public CategoryResponse update(String id, UpdateCategoryRequest request) {
        Category category = findOwned(id);
        ensureNameAvailable(category.getOwnerId(), Category.normalizeName(request.name()), category.getId());
        ensureApplicabilityChangeAllowed(category, request.applicability());
        category.rename(request.name());
        category.changeApplicability(request.applicability());
        try {
            return CategoryResponse.from(repository.save(category));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateName();
        }
    }

    @Transactional
    public CategoryResponse deactivate(String id) {
        Category category = findOwned(id);
        category.deactivate();
        return CategoryResponse.from(repository.save(category));
    }

    @Transactional
    public CategoryResponse activate(String id) {
        Category category = findOwned(id);
        ensureNameAvailable(category.getOwnerId(), category.getNormalizedName(), category.getId());
        category.activate();
        try {
            return CategoryResponse.from(repository.save(category));
        } catch (DataIntegrityViolationException exception) {
            throw duplicateName();
        }
    }

    private Category findOwned(String id) {
        validateId(id);
        return repository.findByIdAndOwnerId(id, ownerId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    private String ownerId() {
        return currentUser.id().toString();
    }

    private void ensureApplicabilityChangeAllowed(Category category, CategoryApplicability requested) {
        if (requested == category.getApplicability()) return;
        String owner = category.getOwnerId();
        if (requested == CategoryApplicability.INCOME
            && transactions.existsByOwnerIdAndCategoryIdAndType(owner, category.getId(), TransactionType.EXPENSE)) {
            throw new ResourceConflictException("Category applicability conflicts with existing expenses.");
        }
        if (requested == CategoryApplicability.EXPENSE
            && transactions.existsByOwnerIdAndCategoryIdAndType(owner, category.getId(), TransactionType.INCOME)) {
            throw new ResourceConflictException("Category applicability conflicts with existing incomes.");
        }
    }

    private void ensureNameAvailable(String ownerId, String normalizedName, String excludedId) {
        boolean exists = excludedId == null
            ? repository.existsByOwnerIdAndNormalizedNameAndActiveTrue(ownerId, normalizedName)
            : repository.existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(ownerId, normalizedName, excludedId);
        if (exists) throw duplicateName();
    }

    private static ResourceConflictException duplicateName() {
        return new ResourceConflictException("An active category with this name already exists.");
    }

    private static void validateId(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Category id must be a valid UUID.", exception);
        }
    }

    private static List<CategoryApplicability> applicableValues(CategoryApplicability context) {
        return context == CategoryApplicability.EXPENSE
            ? List.of(CategoryApplicability.EXPENSE, CategoryApplicability.BOTH)
            : context == CategoryApplicability.INCOME
                ? List.of(CategoryApplicability.INCOME, CategoryApplicability.BOTH)
                : List.of(CategoryApplicability.BOTH);
    }

    private static boolean supports(Category category, CategoryApplicability context) {
        return category.getApplicability() == context || category.getApplicability() == CategoryApplicability.BOTH;
    }
}
