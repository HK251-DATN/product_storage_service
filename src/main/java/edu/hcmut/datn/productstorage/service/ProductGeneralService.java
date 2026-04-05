package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.messaging.productgeneral.ProductGeneralCreatedEvent;

public interface ProductGeneralService {

    ProductGeneral create(ProductGeneral productGeneral);

    ProductGeneral read(Long productGeneralId);

    List<ProductGeneral> readAll(Integer pageNum, Integer pageSize);

    ProductGeneral update(Long productGeneralId, ProductGeneral productGeneral);

    void delete(Long productGeneralId);

    ProductGeneral create(ProductGeneralCreatedEvent event);

    List<ProductGeneral> getSuitableForBatch(Long batchId);
}
