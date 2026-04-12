package edu.hcmut.datn.productstorage.service;

public interface BackOfficeServiceClient {
    void updatePackagingProgress(Long orderId, int progress);
}
