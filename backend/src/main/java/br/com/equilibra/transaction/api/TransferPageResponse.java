package br.com.equilibra.transaction.api;
import java.util.List;
public record TransferPageResponse(List<TransferResponse> content,int page,int size,long totalElements,int totalPages) {}
