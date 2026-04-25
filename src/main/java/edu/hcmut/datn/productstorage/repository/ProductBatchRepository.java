package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.ProductBatch;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

}
