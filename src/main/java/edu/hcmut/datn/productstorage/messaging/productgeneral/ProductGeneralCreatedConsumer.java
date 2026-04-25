package edu.hcmut.datn.productstorage.messaging.productgeneral;

import edu.hcmut.datn.productstorage.service.ProductGeneralService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ProductGeneralCreatedConsumer {

    private final ProductGeneralService productGeneralService;

    @KafkaListener(topics = "product-general-events")
    public void consume(ProductGeneralCreatedEvent event) {
        log.info("Received event: {}", event);

        try {
            productGeneralService.create(event.toProductGeneralEntity());

            log.info("Create product general {} success", event.getProdGenId());
        } catch (Exception e) {
            log.error("Create product general {} fail due to: {}", event.getProdGenId(), e.getMessage());
        }
    }
}
