package br.com.equilibra.account.infrastructure;

import br.com.equilibra.account.domain.AssetAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAccountRepository extends JpaRepository<AssetAccount, String> {

    Optional<AssetAccount> findByIdAndOwnerId(String id, String ownerId);

    List<AssetAccount> findAllByOwnerIdOrderByNameAsc(String ownerId);

    List<AssetAccount> findAllByOwnerIdAndActiveTrueOrderByNameAsc(String ownerId);

    boolean existsByOwnerIdAndNormalizedNameAndActiveTrue(String ownerId, String normalizedName);

    boolean existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(
        String ownerId,
        String normalizedName,
        String id
    );
}
