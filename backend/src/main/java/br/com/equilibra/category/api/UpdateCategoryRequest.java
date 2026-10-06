package br.com.equilibra.category.api;

import br.com.equilibra.category.domain.CategoryApplicability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
    @NotBlank @Size(max = 100) String name,
    @NotNull CategoryApplicability applicability
) {}
