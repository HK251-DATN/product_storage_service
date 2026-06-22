package edu.hcmut.datn.productstorage.messaging.provider;

public record ProviderCreatedEvent(
        Long userId,
        String verificationMethod,
        String fName,
        String lName,
        String avtUrl
) {}
