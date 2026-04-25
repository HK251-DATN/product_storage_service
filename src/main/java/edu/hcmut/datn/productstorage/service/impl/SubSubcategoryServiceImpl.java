package edu.hcmut.datn.productstorage.service.impl;

import edu.hcmut.datn.productstorage.dao.SubSubcategory;
import edu.hcmut.datn.productstorage.repository.SubSubcategoryRepository;
import edu.hcmut.datn.productstorage.service.SubSubcategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubSubcategoryServiceImpl implements SubSubcategoryService {
    
    private final SubSubcategoryRepository subSubcategoryRepository;
    
    @Override
    @Transactional
    public SubSubcategory create(SubSubcategory subSubcategory) {
        return subSubcategoryRepository.save(subSubcategory);
    }
    
    @Override
    public SubSubcategory read(Long subSubcategoryId) {
        return subSubcategoryRepository.findById(subSubcategoryId)
                .orElseThrow(() -> new RuntimeException("Sub-subcategory not found: " + subSubcategoryId));
    }
    
    @Override
    public List<SubSubcategory> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return subSubcategoryRepository.findAll(pageable).toList();
    }
}