package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.ProductDetail;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {

}
