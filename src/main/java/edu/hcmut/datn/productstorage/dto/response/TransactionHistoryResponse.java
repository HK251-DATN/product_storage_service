package edu.hcmut.datn.productstorage.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.TransactionStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponse {
    private Long id;
    private ProviderVerificationType batchType;
    private Long demandId;
    private Long subSubcategoryId;
    private String subSubcategoryName;
    private Long quantity;
    private Unit unit;
    private LocalDate dateNeed;
    private Long unitPrice;
    private TransactionStatus status;
    private Long providerId;
    private LocalDateTime receivedAt;
    private LocalDateTime createdAt;
}
