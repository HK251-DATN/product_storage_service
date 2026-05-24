package edu.hcmut.datn.productstorage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.dao.ProductBatch;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long>, JpaSpecificationExecutor<ProductBatch> {
    Optional<ProductBatch> findByRawProductDemandIdAndProviderId(Long rawProductDemandId, Long providerId);
    List<ProductBatch> findByRawProductDemandIdAndProviderIdIsNullAndVerificationType(Long rawProductDemandId, ProviderVerificationType verificationType);
    List<ProductBatch> findByRawProductDemandId(Long rawProductDemandId);
    List<ProductBatch> findByProcessStatus(ProductBatchProcessStatus processStatus);
    List<ProductBatch> findByRawProductDemandIdAndProcessStatus(Long rawProductDemandId, ProductBatchProcessStatus processStatus);
    List<ProductBatch> findByRawProductDemandIdNotNullAndVerificationType(ProviderVerificationType verificationType);
}
