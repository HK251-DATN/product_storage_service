package edu.hcmut.datn.productstorage.repository.projector;

public interface PickListItem {
    String getName();
    String getUnit();
    Long getBatchId();
    Long getProdGenId();
    Long getProductDetailId();
    Long getOrderItemId();
    Long getBatchDetailId();
    Long getBuyerId();
    Long getOrderId();
}
