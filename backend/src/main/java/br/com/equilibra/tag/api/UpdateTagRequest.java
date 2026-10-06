package br.com.equilibra.tag.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTagRequest(@NotBlank @Size(max = 100) String name) {}
