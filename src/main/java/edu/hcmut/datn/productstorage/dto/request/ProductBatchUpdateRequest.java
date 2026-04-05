package edu.hcmut.datn.productstorage.dto.request;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatchUpdateRequest {

    private Long quantity;
    private Unit unit;
    private String note;
    private LocalDateTime receivedAt;
    private LocalDateTime expiredAt;
    private Long providerId;
    private Long subSubcategoryId;
    private ProductBatchProcessStatus processStatus;

    public ProductBatch toEntity() {
        ProductBatch productBatch = new ProductBatch(quantity, unit, note, receivedAt, expiredAt, providerId, subSubcategoryId);
        if (processStatus != null) {
            productBatch.setProcessStatus(processStatus);
        }
        return productBatch;
    }
}
