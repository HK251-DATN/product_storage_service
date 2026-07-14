package edu.hcmut.datn.productstorage.repository;

import java.util.Optional;

import edu.hcmut.datn.productstorage.dao.SubSubcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubSubcategoryRepository extends JpaRepository<SubSubcategory, Long> {
    Optional<SubSubcategory> findByName(String name);
}