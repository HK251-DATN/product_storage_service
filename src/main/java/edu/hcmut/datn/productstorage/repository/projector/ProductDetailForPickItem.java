package edu.hcmut.datn.productstorage.repository.projector;

public interface ProductDetailForPickItem {
    Long getProdDetailId();
    String getStatus();
    Long getWarehouseId();
    String getWarehouseAddress();
    Long getStorageToolId();
    Long getToolType();
    Long getRackId();
    Long getFridgeId();
}

//prod_detail_id
//status
//warehouse_id
//warehouse_address
//storage_tool_id
//tool_type
//rack_id
//fridge_id