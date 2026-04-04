package edu.hcmut.datn.productstorage.repository;

import edu.hcmut.datn.productstorage.dao.SubSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubSubcategoryRepository extends JpaRepository<SubSubcategory, Long> {
}