package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.Rack;

public interface RackRepository extends JpaRepository<Rack, Long> {

    boolean existsByStorageToolId(Long storageToolId);
}
