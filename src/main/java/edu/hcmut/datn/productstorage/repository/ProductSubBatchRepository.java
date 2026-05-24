package edu.hcmut.datn.productstorage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;

public interface ProductSubBatchRepository extends JpaRepository<ProductSubBatch, Long> {
    List<ProductSubBatch> findByProductBatchId(Long productBatchId);
    List<ProductSubBatch> findByProductBatchIdAndProcessStatusIn(Long productBatchId, List<ProductBatchProcessStatus> statuses);
    List<ProductSubBatch> findByRawProductDemandId(Long rawProductDemandId);
    Optional<ProductSubBatch> findByProviderIdAndRawProductDemandIdAndProductBatchIdAndProcessStatusIn(
            Long providerId, Long rawProductDemandId, Long productBatchId, List<ProductBatchProcessStatus> statuses);
    List<ProductSubBatch> findByRawProductDemandIdNotNull();
}
