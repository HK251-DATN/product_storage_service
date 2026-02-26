package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductDetail;

public interface ProductDetailService {

    ProductDetail create(ProductDetail fridge);

    ProductDetail read(Long fridgeId);

    List<ProductDetail> readAll(Integer pageNum, Integer pageSize);

    ProductDetail update(Long fridgeId, ProductDetail fridge);

    void delete(Long fridgeId);    
}
