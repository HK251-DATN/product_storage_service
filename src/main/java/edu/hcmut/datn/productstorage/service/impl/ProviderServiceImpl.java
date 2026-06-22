package edu.hcmut.datn.productstorage.service.impl;

import org.springframework.stereotype.Service;
import edu.hcmut.datn.productstorage.dao.Provider;
import edu.hcmut.datn.productstorage.exception.ProviderNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProviderRepository;
import edu.hcmut.datn.productstorage.service.ProviderService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderRepository providerRepository;

    @Override
    public Provider create(Provider provider) {
        return providerRepository.save(provider);
    }

    @Override
    public Provider read(Long providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException("Provider not found with id: " + providerId));
    }
    
    @Override
    public Provider update (Long providerId, Provider provider) {
        Provider curProvider = read(providerId);
        
        if (provider.getCertificateType() != null) {
            curProvider.setCertificateType(provider.getCertificateType());
        }
        if (provider.getVerificationMethod() != null) {
            curProvider.setVerificationMethod(provider.getVerificationMethod());
        }
        if (provider.getVerificationStatus() != null) {
            curProvider.setVerificationStatus(provider.getVerificationStatus());
        }
        
        return providerRepository.save(curProvider);
    }
}
