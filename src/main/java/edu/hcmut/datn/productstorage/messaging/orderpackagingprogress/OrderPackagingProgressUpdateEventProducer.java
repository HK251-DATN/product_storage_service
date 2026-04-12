package edu.hcmut.datn.productstorage.messaging.orderpackagingprogress;

import edu.hcmut.datn.productstorage.messaging.batchdetail.BatchDetailCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderPackagingProgressUpdateEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderPackagingProgressUpdateEvent(OrderPackagingProgressUpdateEvent event) {
        kafkaTemplate.send(
                "order-packaging-progress-update-events",
                event.orderId().toString(), event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event", ex);
            } else {
                log.info("Event sent with offset {}",
                        result.getRecordMetadata().offset()
                );
            }
        });
    }
}
