package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import edu.hcmut.datn.productstorage.dao.ProductDetail;
import edu.hcmut.datn.productstorage.exception.ProductDetailNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductDetailRepository;
import edu.hcmut.datn.productstorage.service.ProductDetailService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductDetailServiceImpl implements ProductDetailService {

    private final ProductDetailRepository productDetailRepository;

    @Override
    public ProductDetail create(ProductDetail productDetail) {
        return productDetailRepository.save(productDetail);
    }

    @Override
    public ProductDetail read(Long productDetailId) {
        return productDetailRepository.findById(productDetailId).orElseThrow(() -> new ProductDetailNotFoundException("Product detail not found"));
    }

    @Override
    public List<ProductDetail> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return productDetailRepository.findAll(pageable).toList();
    }

    @Override
    public ProductDetail update(Long productDetailId, ProductDetail productDetail) {
        ProductDetail curProductDetail = read(productDetailId);

        if (productDetail.getStatus() != null) {
            curProductDetail.setStatus(productDetail.getStatus());
        }
        if (productDetail.getPrice() != null) {
            curProductDetail.setPrice(productDetail.getPrice());
        }
        if (productDetail.getNumOfStar() != null) {
            curProductDetail.setNumOfStar(productDetail.getNumOfStar());
        }
        if (productDetail.getStorageToolId() != null) {
            curProductDetail.setStorageToolId(productDetail.getStorageToolId());
        }
        if (productDetail.getBatchId() != null) {
            curProductDetail.setBatchId(productDetail.getBatchId());
        }
        if (productDetail.getProdGenId() != null) {
            curProductDetail.setProdGenId(productDetail.getProdGenId());
        }
        if (productDetail.getUnit() != null) {
            curProductDetail.setUnit(productDetail.getUnit());
        }
        if (productDetail.getUnitQuantity() != null) {
            curProductDetail.setUnitQuantity(productDetail.getUnitQuantity());
        }

        return productDetailRepository.save(curProductDetail);
    }

    @Override
    public void delete(Long productDetailId) {
        ProductDetail productDetail = read(productDetailId);

        productDetailRepository.delete(productDetail);
    }

}
