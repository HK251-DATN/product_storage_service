package edu.hcmut.datn.productstorage.service.impl;

import edu.hcmut.datn.productstorage.dao.Category;
import edu.hcmut.datn.productstorage.repository.CategoryRepository;
import edu.hcmut.datn.productstorage.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public Category create(Category category) {
        return categoryRepository.save(category);
    }
}
