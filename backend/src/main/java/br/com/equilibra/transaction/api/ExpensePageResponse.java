package br.com.equilibra.transaction.api;

import java.util.List;

public record ExpensePageResponse(
    List<ExpenseResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
