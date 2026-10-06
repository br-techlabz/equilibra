package br.com.equilibra.tag.api;

import br.com.equilibra.tag.domain.Tag;
import java.time.Instant;

public record TagResponse(String id, String name, boolean active, Instant createdAt, Instant updatedAt) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.isActive(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
