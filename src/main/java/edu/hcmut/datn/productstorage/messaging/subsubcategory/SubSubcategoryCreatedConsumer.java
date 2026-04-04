package edu.hcmut.datn.productstorage.messaging.subsubcategory;

import edu.hcmut.datn.productstorage.dao.SubSubcategory;
import edu.hcmut.datn.productstorage.service.SubSubcategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubSubcategoryCreatedConsumer {
    
    private final SubSubcategoryService subSubcategoryService;
    
    @KafkaListener(topics = "subsubcategory-events")
    @Transactional
    public void consume(SubSubcategoryCreatedEvent event) {
        log.info("Received SubSubcategoryCreatedEvent: subSubcategoryId={}, name={}",
                event.subSubcategoryId(), event.name());
        
        try {
            // Convert event to SubSubcategory entity
            SubSubcategory subSubcategory = new SubSubcategory();
            subSubcategory.setSubSubcategoryId(event.subSubcategoryId());
            subSubcategory.setName(event.name());
            subSubcategory.setDescription(event.description());
            subSubcategory.setIconUrl(event.iconUrl());
            // Note: We don't store subcategoryId (parent reference) as we only need the finest detail
            
            // Save sub-subcategory
            subSubcategoryService.create(subSubcategory);
            
            log.info("Successfully created sub-subcategory {} in product-storage service",
                    event.subSubcategoryId());
            
        } catch (Exception e) {
            log.error("Failed to create sub-subcategory {} in product-storage service: {}",
                    event.subSubcategoryId(), e.getMessage(), e);
            throw e; // Re-throw to trigger Kafka retry if configured
        }
    }
}