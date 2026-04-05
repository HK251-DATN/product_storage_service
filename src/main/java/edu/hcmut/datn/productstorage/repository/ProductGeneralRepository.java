package edu.hcmut.datn.productstorage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.productstorage.dao.ProductGeneral;

public interface ProductGeneralRepository extends JpaRepository<ProductGeneral, Long>{

    List<ProductGeneral> findBySubSubcategoryId(Long subSubcategoryId);

}
