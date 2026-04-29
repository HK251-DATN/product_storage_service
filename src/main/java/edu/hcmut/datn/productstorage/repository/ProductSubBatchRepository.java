package edu.hcmut.datn.productstorage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;

public interface ProductSubBatchRepository extends JpaRepository<ProductSubBatch, Long> {
    List<ProductSubBatch> findByProductBatchId(Long productBatchId);
    List<ProductSubBatch> findByRawProductDemandId(Long rawProductDemandId);
}
