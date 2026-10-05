package br.com.equilibra.account.application;

import br.com.equilibra.account.api.AssetAccountResponse;
import br.com.equilibra.account.api.CreateAssetAccountRequest;
import br.com.equilibra.account.api.UpdateAssetAccountRequest;
import br.com.equilibra.account.domain.AssetAccount;
import br.com.equilibra.account.infrastructure.AssetAccountRepository;
import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AssetAccountService {

    private final AssetAccountRepository repository;
    private final CurrentUser currentUser;

    public AssetAccountService(AssetAccountRepository repository, CurrentUser currentUser) {
        this.repository = repository;
        this.currentUser = currentUser;
    }

    @Transactional
    public AssetAccountResponse create(CreateAssetAccountRequest request) {
        String ownerId = currentUser.id().toString();
        String normalizedName = AssetAccount.normalizeName(request.name());
        ensureNameAvailable(ownerId, normalizedName, null);

        try {
            return AssetAccountResponse.from(repository.save(new AssetAccount(
                ownerId,
                request.name(),
                request.type(),
                request.initialBalance()
            )));
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("An active asset account with this name already exists.");
        }
    }

    @Transactional(readOnly = true)
    public List<AssetAccountResponse> list(boolean includeInactive) {
        String ownerId = currentUser.id().toString();
        List<AssetAccount> accounts = includeInactive
            ? repository.findAllByOwnerIdOrderByNameAsc(ownerId)
            : repository.findAllByOwnerIdAndActiveTrueOrderByNameAsc(ownerId);
        return accounts.stream().map(AssetAccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AssetAccountResponse get(String id) {
        return AssetAccountResponse.from(findOwned(id));
    }

    @Transactional
    public AssetAccountResponse update(String id, UpdateAssetAccountRequest request) {
        AssetAccount account = findOwned(id);
        String normalizedName = AssetAccount.normalizeName(request.name());
        if (account.isActive()) {
            ensureNameAvailable(account.getOwnerId(), normalizedName, account.getId());
        }
        account.rename(request.name());
        account.changeType(request.type());
        account.changeInitialBalance(request.initialBalance());
        try {
            return AssetAccountResponse.from(repository.save(account));
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("An active asset account with this name already exists.");
        }
    }

    @Transactional
    public AssetAccountResponse deactivate(String id) {
        AssetAccount account = findOwned(id);
        account.deactivate();
        return AssetAccountResponse.from(repository.save(account));
    }

    @Transactional
    public AssetAccountResponse activate(String id) {
        AssetAccount account = findOwned(id);
        ensureNameAvailable(account.getOwnerId(), account.getNormalizedName(), account.getId());
        account.activate();
        try {
            return AssetAccountResponse.from(repository.save(account));
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceConflictException("An active asset account with this name already exists.");
        }
    }

    private AssetAccount findOwned(String id) {
        UUID.fromString(id);
        return repository.findByIdAndOwnerId(id, currentUser.id().toString())
            .orElseThrow(() -> new ResourceNotFoundException("Asset account not found."));
    }

    private void ensureNameAvailable(String ownerId, String normalizedName, String excludedId) {
        boolean exists = excludedId == null
            ? repository.existsByOwnerIdAndNormalizedNameAndActiveTrue(ownerId, normalizedName)
            : repository.existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(ownerId, normalizedName, excludedId);
        if (exists) {
            throw new ResourceConflictException("An active asset account with this name already exists.");
        }
    }
}
