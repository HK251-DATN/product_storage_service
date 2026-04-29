package edu.hcmut.datn.productstorage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.hcmut.datn.productstorage.dao.Provider;

public interface ProviderRepository extends JpaRepository<Provider, Long> {
    Optional<Provider> findByProviderId(Long providerId);
}
