package edu.hcmut.datn.productstorage.service.impl;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.messaging.orderpackagingprogress.OrderPackagingProgressUpdateEvent;
import edu.hcmut.datn.productstorage.messaging.orderpackagingprogress.OrderPackagingProgressUpdateEventProducer;
import edu.hcmut.datn.productstorage.messaging.orderpick.OrderPickRequestedEvent;
import edu.hcmut.datn.productstorage.repository.OrderItemRepository;
import edu.hcmut.datn.productstorage.service.PickListService;

import edu.hcmut.datn.productstorage.common.enums.ProductStatus;
import edu.hcmut.datn.productstorage.dao.ProductDetail;
import edu.hcmut.datn.productstorage.repository.ProductDetailRepository;
import edu.hcmut.datn.productstorage.service.BackOfficeServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PickListServiceImpl implements PickListService {

    private final OrderItemRepository orderItemRepository;
    private final ProductDetailRepository productDetailRepository;
    private final OrderPackagingProgressUpdateEventProducer orderPackagingProgressUpdateEventProducer;

    @Override
    public void createPickList(OrderPickRequestedEvent event) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderPickRequestedEvent.OrderItemInfo itemInfo : event.getOrderItems()) {
            for (int i = 0; i < itemInfo.quantity(); i++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(event.getOrderId());
                orderItem.setBatchDetailId(Long.valueOf(itemInfo.batchDetailId()));
                orderItem.setBuyerId(event.getBuyerId());
                orderItems.add(orderItem);
            }
        }
        orderItemRepository.saveAll(orderItems);
    }

    @Override
    public List<OrderItem> getPickList(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public void linkOrderItem(Long orderItemId, Long productDetailId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("OrderItem not found"));

        ProductDetail productDetail = productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new RuntimeException("ProductDetail not found"));

        if (productDetail.getStatus() != ProductStatus.STORED) {
            throw new RuntimeException("Product is not available for picking");
        }

        orderItem.setProductDetailId(productDetailId);
        productDetail.setStatus(ProductStatus.PICKED);

        orderItemRepository.save(orderItem);
        productDetailRepository.save(productDetail);

        // Calculate progress
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderItem.getOrderId());
        long linkedItems = orderItems.stream().filter(item -> item.getProductDetailId() != null).count();
        int progress = (int) (((double) linkedItems / orderItems.size()) * 100);

        // Update back-office
        orderPackagingProgressUpdateEventProducer.publishOrderPackagingProgressUpdateEvent(
                new OrderPackagingProgressUpdateEvent(orderItem.getOrderId(), progress)
        );
    }
}
