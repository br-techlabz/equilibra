package br.com.equilibra.transaction.api;
import java.util.List;
public record TransactionHistoryPageResponse(List<TransactionHistoryResponse> content,int page,int size,long totalElements,int totalPages){}
