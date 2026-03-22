package edu.hcmut.datn.productstorage.messaging.productgeneral;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProductGeneralCreatedEvent {

    @Getter
    private Long prodGenId;

    @Getter
    private String prodName;

    @Getter
    private String imgUrl;

    @Getter
    private String description;

    @Getter
    private Long categoryId;

    public ProductGeneral toProductGeneralEntity() {
        ProductGeneral newProduct = new ProductGeneral();

        newProduct.setProdGenId(prodGenId);
        newProduct.setName(prodName);
        newProduct.setImgUrl(imgUrl);
        newProduct.setDescription(description);
        newProduct.setCategoryId(categoryId);

        return newProduct;
    }
}
