package edu.hcmut.datn.productstorage.service;

import java.util.List;

import edu.hcmut.datn.productstorage.common.enums.Unit;
import edu.hcmut.datn.productstorage.dao.ProductDetail;

public interface ProductDetailService {

    ProductDetail create(ProductDetail productDetail);

    ProductDetail read(Long productDetailId);

    List<ProductDetail> readAll(Integer pageNum, Integer pageSize);

    ProductDetail update(Long productDetailId, ProductDetail productDetail);

    void delete(Long productDetailId);

    List<ProductDetail> processProductBatch(ProductDetail productDetail);
}
