package edu.hcmut.datn.productstorage.messaging.provider;

import edu.hcmut.datn.productstorage.common.enums.CertificateType;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.VerificationStatus;
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
public class ProviderVerificationUpdatedConsumer {

    private final ProviderService providerService;

    @KafkaListener(topics = "provider-verification-update-events")
    @Transactional
    public void consume(ProviderVerificationUpdatedEvent event) {
        log.info("Received ProviderVerificationUpdatedEvent: providerId={}, status={}, method={}, certificateType={}",
                event.providerId(), event.verificationStatus(), event.verificationMethod(), event.certificateType());

        try {
            // Try to find existing provider
            Provider provider = providerService.read(event.providerId());

            // Update fields
            if (event.verificationStatus() != null) {
                provider.setVerificationStatus(VerificationStatus.valueOf(event.verificationStatus()));
            }

            if (event.verificationMethod() != null) {
                provider.setVerificationMethod(ProviderVerificationType.valueOf(event.verificationMethod()));
            }

            if (event.certificateType() != null) {
                provider.setCertificateType(CertificateType.valueOf(event.certificateType()));
            } else {
                // Clear certificate type if null (for VIDEO verification)
                provider.setCertificateType(null);
            }

            // Update logo URL
            if (event.logoUrl() != null) {
                provider.setLogoUrl(event.logoUrl());
            }

            providerService.update(provider.getProviderId(), provider);

            log.info("Successfully updated provider {} verification in product-storage service",
                    event.providerId());
        } catch (Exception e) {
            log.error("Failed to update provider {} verification in product-storage service: {}",
                    event.providerId(), e.getMessage(), e);
        }
    }
}
