package edu.hcmut.datn.productstorage.dto.request;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductGeneralCreateRequest {
    private Long prodGenId;
    private String name;

    public ProductGeneral toEntity() {
        return new ProductGeneral(prodGenId, name);
    }
}
