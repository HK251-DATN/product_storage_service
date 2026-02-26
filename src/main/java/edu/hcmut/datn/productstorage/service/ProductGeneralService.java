package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;

public interface ProductGeneralService {

    ProductGeneral create(ProductGeneral fridge);

    ProductGeneral read(Long fridgeId);

    List<ProductGeneral> readAll(Integer pageNum, Integer pageSize);

    ProductGeneral update(Long fridgeId, ProductGeneral fridge);

    void delete(Long fridgeId);    
}
