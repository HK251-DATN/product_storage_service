package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository productBatchRepository;

    @Override
    public ProductBatch create(ProductBatch fridge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ProductBatch read(Long fridgeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProductBatch> readAll(Integer pageNum, Integer pageSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ProductBatch update(Long fridgeId, ProductBatch fridge) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Long fridgeId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
