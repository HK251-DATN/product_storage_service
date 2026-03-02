package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.Fridge;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    boolean existsByStorageToolId(Long storageToolId);
}
