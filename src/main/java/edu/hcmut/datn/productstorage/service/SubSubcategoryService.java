package edu.hcmut.datn.productstorage.service;

import edu.hcmut.datn.productstorage.dao.SubSubcategory;

import java.util.List;

public interface SubSubcategoryService {
    SubSubcategory create(SubSubcategory subSubcategory);
    SubSubcategory read(Long subSubcategoryId);
    List<SubSubcategory> readAll(Integer pageNum, Integer pageSize);
}