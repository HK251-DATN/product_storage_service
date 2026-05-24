package edu.hcmut.datn.productstorage.messaging.provider;

import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.dao.Provider;
import edu.hcmut.datn.productstorage.service.ProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProviderCreatedConsumer {

    private final ProviderService providerService;

    @KafkaListener(topics = "provider-create-events")
    @Transactional
    public void consume(ProviderCreatedEvent event) {
        log.info("Received ProviderCreatedEvent: providerId={}, verificationMethod={}",
                event.userId(), event.verificationMethod());

        try {
            Provider provider = new Provider();
            provider.setProviderId(event.userId());

            if (event.verificationMethod() != null && !event.verificationMethod().isEmpty()) {
                provider.setVerificationMethod(ProviderVerificationType.valueOf(event.verificationMethod()));
            }
            provider.setFName(event.fName());
            provider.setLName(event.lName());
            provider.setAvtUrl(event.avtUrl());

            // Initial status is UNVERIFIED (set by default in Provider constructor)
            // Verification status and certificate type will be updated later via
            // provider-verification-update-events when admin approves the provider

            providerService.create(provider);

            log.info("Successfully created provider {} in product-storage service",
                    event.userId());
        } catch (Exception e) {
            log.error("Failed to create provider {} in product-storage service: {}",
                    event.userId(), e.getMessage(), e);
            throw e;
        }
    }
}
