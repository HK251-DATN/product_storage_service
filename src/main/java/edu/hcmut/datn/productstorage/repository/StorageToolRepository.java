package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.StorageTool;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StorageToolRepository extends JpaRepository<StorageTool, Long> {

    @Query(
            value = """
                    SELECT      *
                    FROM        STORAGE_TOOLS ST
                    WHERE       (:warehouseId = 0 OR ST.WAREHOUSE_ID = :warehouseId)
                    LIMIT       :pageSize
                    OFFSET      (:pageNum - 1) * :pageSize;
                    """,
            nativeQuery = true
    )
    List<StorageTool> search(
            @Param("warehouseId") Long warehouseId,
            @Param("pageNum") Integer pageNum,
            @Param("pageSize") Integer pageSize
    );
}
