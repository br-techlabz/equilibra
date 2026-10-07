package br.com.equilibra.tag.infrastructure;

import br.com.equilibra.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String> {
    Optional<Tag> findByIdAndOwnerId(String id, String ownerId);
    List<Tag> findAllByOwnerIdAndActiveTrueOrderByNameAsc(String ownerId);
    List<Tag> findAllByOwnerIdOrderByNameAsc(String ownerId);
    boolean existsByOwnerIdAndNormalizedNameAndActiveTrue(String ownerId, String normalizedName);
    boolean existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(String ownerId, String normalizedName, String id);
    List<Tag> findAllByOwnerIdAndIdIn(String ownerId, Collection<String> ids);
}
