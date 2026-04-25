package edu.hcmut.datn.productstorage.messaging.batchdetail;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchDetailProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBatchDetailCreated(BatchDetailCreateEvent event) {
        kafkaTemplate.send("batch-detail-events", event.batchDetailId().toString(), event).whenComplete((result, ex) -> {
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

