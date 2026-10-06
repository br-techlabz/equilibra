package br.com.equilibra.category.infrastructure;

import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByIdAndOwnerId(String id, String ownerId);

    List<Category> findAllByOwnerIdOrderByNameAsc(String ownerId);

    List<Category> findAllByOwnerIdAndActiveTrueOrderByNameAsc(String ownerId);

    boolean existsByOwnerIdAndNormalizedNameAndActiveTrue(String ownerId, String normalizedName);

    boolean existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(
        String ownerId, String normalizedName, String id
    );

    List<Category> findAllByOwnerIdAndActiveTrueAndApplicabilityInOrderByNameAsc(
        String ownerId, Collection<CategoryApplicability> applicability
    );
}
