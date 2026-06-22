package edu.hcmut.datn.productstorage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;

public interface RawProductDemandRepository extends JpaRepository<RawProductDemand, Long> {
    List<RawProductDemand> findByStatus(RawProductDemandStatus status);
    List<RawProductDemand> findBySubSubcategoryId(Long subSubcategoryId);
    List<RawProductDemand> findBySubSubcategoryIdAndStatus(Long subSubcategoryId, RawProductDemandStatus status);
}
