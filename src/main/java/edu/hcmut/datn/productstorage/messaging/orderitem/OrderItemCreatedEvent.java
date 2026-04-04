package edu.hcmut.datn.productstorage.messaging.orderitem;

import java.math.BigDecimal;

public record OrderItemCreatedEvent(
        Long orderItemId,
        Long orderId,
        Long batchDetailId,
        Long quantity,
        BigDecimal unitPriceAtPurchase,
        String buyerId
) {}