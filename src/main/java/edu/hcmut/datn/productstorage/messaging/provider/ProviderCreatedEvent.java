package edu.hcmut.datn.productstorage.messaging.provider;

public record ProviderCreatedEvent(
        Long userId,
        String verificationMethod
) {}
