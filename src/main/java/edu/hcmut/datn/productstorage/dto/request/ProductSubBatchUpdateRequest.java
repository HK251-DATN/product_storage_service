package edu.hcmut.datn.productstorage.dto.request;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSubBatchUpdateRequest {

    private Long quantity;
    private Unit unit;
    private String note;
    private LocalDateTime receivedAt;
    private LocalDateTime expiredAt;
    private Long providerId;
    private Long subSubcategoryId;
    private Long productBatchId;
    private Long rawProductDemandId;
    private ProductBatchProcessStatus processStatus;

    public ProductSubBatch toEntity() {
        ProductSubBatch productSubBatch = new ProductSubBatch(quantity, unit, note, receivedAt, expiredAt, providerId, subSubcategoryId, productBatchId, rawProductDemandId);
        if (processStatus != null) {
            productSubBatch.setProcessStatus(processStatus);
        }
        return productSubBatch;
    }
}
