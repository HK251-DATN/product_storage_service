package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.ProductDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    
    @Query(value = """
            SELECT COUNT(*)
            FROM product_details
            WHERE
                BATCH_ID = :batchId
                AND STATUS = 'STORED'
            """, nativeQuery = true)
    Integer countStoredProductDetailOfTheSameBatch(@Param("batchId") Long batchId);
}
