package edu.hcmut.datn.productstorage.messaging.orderitem;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemCreatedConsumer {
    
    private final OrderItemRepository orderItemRepository;
    
    @KafkaListener(
            topics = "order-item-events"
    )
    @Transactional
    public void consumeOrderItemCreated(OrderItemCreatedEvent event) {
        try {
            log.info("Received OrderItemCreatedEvent: orderItemId={}, orderId={}, batchDetailId={}",
                    event.orderItemId(), event.orderId(), event.batchDetailId());
            
            // Check if order item already exists (idempotency)
            if (orderItemRepository.existsByOrderItemId(event.orderItemId())) {
                log.warn("OrderItem {} already exists, skipping creation", event.orderItemId());
                return;
            }
            
            // Create OrderItem entity
            OrderItem orderItem = new OrderItem(
                    event.orderItemId(),
                    event.orderId(),
                    event.batchDetailId(),
                    event.quantity(),
                    event.unitPriceAtPurchase(),
                    event.buyerId()
            );
            
            // Note: productDetailId is null initially
            // It will be set later when the actual product is collected/assigned
            orderItem.setProductDetailId(null);
            
            // Save to database
            orderItemRepository.save(orderItem);
            
            log.info("OrderItem created successfully: orderItemId={}", event.orderItemId());
            
        } catch (Exception e) {
            log.error("Error processing OrderItemCreatedEvent for orderItemId: {}",
                    event.orderItemId(), e);
            throw e; // Re-throw to trigger Kafka retry mechanism
        }
    }
}