package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.common.enums.ProductStatus;
import edu.hcmut.datn.productstorage.dao.ProductDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessProductBatchRequest {
    private ProductStatus status;
    private Long price;
    private Long numOfStar;
    private Long storageToolId;
    private Long batchId;
    private Long prodGenId;
    private Long subBatchId;

    public ProductDetail toEntity() {
        ProductDetail productDetail = new ProductDetail(status, price, numOfStar, storageToolId, batchId, prodGenId);
        productDetail.setSubBatchId(subBatchId);
        return productDetail;
    }
}
