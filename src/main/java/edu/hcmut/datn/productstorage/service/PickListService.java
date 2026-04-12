package edu.hcmut.datn.productstorage.service;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.messaging.orderpick.OrderPickRequestedEvent;

import java.util.List;

public interface PickListService {
    void createPickList(OrderPickRequestedEvent event);
    List<OrderItem> getPickList(Long orderId);
    void linkOrderItem(Long orderItemId, Long productDetailId);
}

