package br.com.equilibra.transaction.api;
import java.util.List;
public record IncomePageResponse(List<IncomeResponse> content,int page,int size,long totalElements,int totalPages) {}
