package edu.hcmut.datn.productstorage.repository;

import edu.hcmut.datn.productstorage.dao.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
