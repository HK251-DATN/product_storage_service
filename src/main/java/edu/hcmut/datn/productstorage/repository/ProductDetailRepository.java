package edu.hcmut.datn.productstorage.repository;

import edu.hcmut.datn.productstorage.repository.projector.ProductDetailForPickItem;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.ProductDetail;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    
    @Query(value = """
            SELECT COUNT(*)
            FROM product_details
            WHERE
                BATCH_ID = :batchId
                AND STATUS = 'STORED'
            """, nativeQuery = true)
    Integer countStoredProductDetailOfTheSameBatch(@Param("batchId") Long batchId);
    
    
    @Query(value = """
            SELECT
                pd.prod_detail_id,
                pd.status,
                w.warehouse_id,
                w.address AS warehouse_address,
                st.storage_tool_id,
                st.tool_type,                  -- 'RACK' or 'FRIDGE'
            
                -- Rack info (null if stored in fridge)
                r.rack_id,
            
                -- Fridge info (null if stored in rack)
                f.fridge_id
            
            FROM product_details pd
            
            	JOIN storage_tools st
            	    ON pd.storage_tool_id = st.storage_tool_id
            	JOIN warehouses w
            	    ON st.warehouse_id = w.warehouse_id
            
            	-- LEFT JOIN both Rack and Fridge; only one will match depending on tool_type
            	LEFT JOIN rack r
            	    ON st.storage_tool_id = r.storage_tool_id
            	LEFT JOIN fridges f
            	    ON st.storage_tool_id = f.storage_tool_id
            
            WHERE
            	pd.batch_id = :batch_id
            	AND pd.status = 'STORED';
            
            """, nativeQuery = true
    )
    List<ProductDetailForPickItem> getProductDetailListForPickItem(
            @Param("batch_id") Long batchId
    );
}
