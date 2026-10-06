package br.com.equilibra.tag.application;

import br.com.equilibra.shared.api.CurrentUser;
import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.tag.api.*;
import br.com.equilibra.tag.domain.Tag;
import br.com.equilibra.tag.infrastructure.TagRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class TagService {
    private final TagRepository repository;
    private final CurrentUser currentUser;
    public TagService(TagRepository repository, CurrentUser currentUser) { this.repository = repository; this.currentUser = currentUser; }

    @Transactional
    public TagResponse create(CreateTagRequest request) {
        String owner = owner();
        ensureAvailable(owner, Tag.normalizeName(request.name()), null);
        try { return TagResponse.from(repository.save(new Tag(owner, request.name()))); }
        catch (DataIntegrityViolationException ex) { throw conflict(); }
    }

    @Transactional(readOnly = true)
    public List<TagResponse> list(boolean includeInactive) {
        String owner = owner();
        List<Tag> tags = includeInactive ? repository.findAllByOwnerIdOrderByNameAsc(owner) : repository.findAllByOwnerIdAndActiveTrueOrderByNameAsc(owner);
        return tags.stream().map(TagResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TagResponse get(String id) { return TagResponse.from(findOwned(id)); }

    @Transactional
    public TagResponse update(String id, UpdateTagRequest request) {
        Tag tag = findOwned(id);
        ensureAvailable(owner(), Tag.normalizeName(request.name()), id);
        tag.rename(request.name());
        try { return TagResponse.from(repository.save(tag)); }
        catch (DataIntegrityViolationException ex) { throw conflict(); }
    }

    @Transactional
    public TagResponse deactivate(String id) { Tag tag = findOwned(id); tag.deactivate(); return TagResponse.from(repository.save(tag)); }

    @Transactional
    public TagResponse activate(String id) {
        Tag tag = findOwned(id);
        ensureAvailable(owner(), tag.getNormalizedName(), id);
        tag.activate();
        try { return TagResponse.from(repository.save(tag)); }
        catch (DataIntegrityViolationException ex) { throw conflict(); }
    }

    private Tag findOwned(String id) {
        validateId(id);
        return repository.findByIdAndOwnerId(id, owner()).orElseThrow(() -> new ResourceNotFoundException("Tag not found."));
    }
    private String owner() { return currentUser.id().toString(); }
    private void ensureAvailable(String owner, String normalized, String excluded) {
        boolean exists = excluded == null ? repository.existsByOwnerIdAndNormalizedNameAndActiveTrue(owner, normalized) : repository.existsByOwnerIdAndNormalizedNameAndActiveTrueAndIdNot(owner, normalized, excluded);
        if (exists) throw conflict();
    }
    private static ResourceConflictException conflict() { return new ResourceConflictException("An active tag with this name already exists."); }
    private static void validateId(String value) { try { UUID.fromString(value); } catch (Exception ex) { throw new IllegalArgumentException("Tag id must be a valid UUID.", ex); } }
}
