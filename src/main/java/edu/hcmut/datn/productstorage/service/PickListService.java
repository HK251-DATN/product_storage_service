package edu.hcmut.datn.productstorage.service;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.messaging.orderpick.OrderPickRequestedEvent;
import edu.hcmut.datn.productstorage.repository.projector.PickListItem;
import edu.hcmut.datn.productstorage.repository.projector.ProductDetailForPickItem;

import java.util.List;

public interface PickListService {
    void createPickList(OrderPickRequestedEvent event);
    List<PickListItem> getPickList(Long orderId);
    void linkOrderItem(Long orderItemId, Long productDetailId);
    Integer getProductDetailCurrentQuantity(Long batchId);
    List<ProductDetailForPickItem> getProductDetailListForPickItem(Long batchId);
}

