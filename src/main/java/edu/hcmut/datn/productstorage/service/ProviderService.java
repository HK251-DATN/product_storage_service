package edu.hcmut.datn.productstorage.service;

import edu.hcmut.datn.productstorage.dao.Provider;

public interface ProviderService {
    Provider create(Provider provider);
    Provider read(Long providerId);
    Provider update(Long providerId, Provider provider);
}
