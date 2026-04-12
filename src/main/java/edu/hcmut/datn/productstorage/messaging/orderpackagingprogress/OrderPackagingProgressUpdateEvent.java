package edu.hcmut.datn.productstorage.messaging.orderpackagingprogress;

public record OrderPackagingProgressUpdateEvent(
        Long orderId,
        Integer packagingProgress
) {
}
