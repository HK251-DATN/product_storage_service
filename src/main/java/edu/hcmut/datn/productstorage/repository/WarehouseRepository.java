package edu.hcmut.datn.productstorage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

}
