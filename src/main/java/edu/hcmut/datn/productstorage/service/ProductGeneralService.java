package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.messaging.productgeneral.ProductGeneralCreatedEvent;

public interface ProductGeneralService {

    ProductGeneral create(ProductGeneral fridge);

    ProductGeneral read(Long fridgeId);

    List<ProductGeneral> readAll(Integer pageNum, Integer pageSize);

    ProductGeneral update(Long fridgeId, ProductGeneral fridge);

    void delete(Long fridgeId);

    ProductGeneral create(ProductGeneralCreatedEvent event);
}
