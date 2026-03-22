package edu.hcmut.datn.productstorage.messaging.category;

import edu.hcmut.datn.productstorage.service.CategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class CategoryCreatedConsumer {

    private final CategoryService categoryService;

    @KafkaListener(topics = "category-events")
    public void consume(CategoryCreatedEvent event) {
        log.info("Received event: {}", event);

        try {
            categoryService.create(event.toCategoryEntity());

            log.info("Create category {} success", event.getCategoryId());
        } catch (Exception e) {
            log.error("Create category {} fail due to: {}", event.getCategoryId(), e.getMessage());
        }
    }
}
