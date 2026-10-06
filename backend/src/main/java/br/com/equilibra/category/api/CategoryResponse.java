package br.com.equilibra.category.api;

import br.com.equilibra.category.domain.Category;
import br.com.equilibra.category.domain.CategoryApplicability;

import java.time.Instant;

public record CategoryResponse(
    String id,
    String name,
    CategoryApplicability applicability,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getApplicability(),
            category.isActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
