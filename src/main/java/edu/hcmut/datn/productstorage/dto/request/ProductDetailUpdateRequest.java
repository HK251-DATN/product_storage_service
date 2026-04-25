package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.common.enums.ProductStatus;
import edu.hcmut.datn.productstorage.dao.ProductDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailUpdateRequest {
    private ProductStatus status;
    private Long price;
    private Long numOfStar;
    private Long storageToolId;
    private Long batchId;
    private Long prodGenId;
    
    public ProductDetail toEntity() {
        return new ProductDetail(status, price, numOfStar, storageToolId, batchId, prodGenId);
    }
}