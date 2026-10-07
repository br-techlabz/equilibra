package br.com.equilibra.transaction.application;

import br.com.equilibra.shared.web.exception.ResourceConflictException;
import br.com.equilibra.shared.web.exception.ResourceNotFoundException;
import br.com.equilibra.tag.domain.Tag;
import br.com.equilibra.tag.infrastructure.TagRepository;
import br.com.equilibra.transaction.domain.FinancialTransaction;
import br.com.equilibra.transaction.domain.TagSummary;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TransactionTagService {
    private final TagRepository tags;
    public TransactionTagService(TagRepository tags) { this.tags = tags; }

    public List<TagSummary> replace(FinancialTransaction transaction, List<String> ids) {
        List<String> requested = ids == null ? List.of() : ids;
        if (new HashSet<>(requested).size() != requested.size()) throw new IllegalArgumentException("tagIds must not contain duplicates");
        List<Tag> resolved = new ArrayList<>();
        for (String id : requested) {
            Tag tag = tags.findByIdAndOwnerId(id, transaction.getOwnerId()).orElseThrow(() -> new ResourceNotFoundException("Tag not found."));
            if (!tag.isActive()) throw new ResourceConflictException("Tag is inactive.");
            resolved.add(tag);
        }
        transaction.replaceTagIds(resolved.stream().map(Tag::getId).toList());
        return summaries(transaction);
    }

    public List<TagSummary> summaries(FinancialTransaction transaction) {
        if (transaction.getTagIds().isEmpty()) return List.of();
        return tags.findAllByOwnerIdAndIdIn(transaction.getOwnerId(), transaction.getTagIds()).stream()
            .sorted(Comparator.comparing(Tag::getName))
            .map(t -> new TagSummary(t.getId(), t.getName(), t.isActive())).toList();
    }
}
