package edu.hcmut.datn.productstorage.messaging.provider;

public record ProviderVerificationUpdatedEvent(
        Long providerId,
        String verificationStatus,
        String verificationMethod,
        String certificateType,  // Nullable - only set for CERTIFICATE method
        String logoUrl  // Provider logo URL
) {}
