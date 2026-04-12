package edu.hcmut.datn.productstorage.messaging.orderpick;

import edu.hcmut.datn.productstorage.service.PickListService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPickRequestedConsumer {
    
    private final PickListService pickListService;

    @KafkaListener(topics = "order-pick-requested-events", groupId = "product-storage-group")
    public void consume(OrderPickRequestedEvent event) {
        log.info("Consumed message -> {}", event);
        try {
            pickListService.createPickList(event);
            log.info("Pick list for order {} created successfully", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to create pick list for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
