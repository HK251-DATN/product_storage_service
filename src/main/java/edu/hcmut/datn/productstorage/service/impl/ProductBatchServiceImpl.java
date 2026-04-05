package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.exception.ProductBatchNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ProductBatchServiceImpl implements ProductBatchService {
    
    private final ProductBatchRepository productBatchRepository;
    
    @Override
    public ProductBatch create(ProductBatch productBatch) {
        return productBatchRepository.save(productBatch);
    }
    
    @Override
    public ProductBatch read(Long productBatchId) {
        return productBatchRepository.findById(productBatchId)
                .orElseThrow(() -> new ProductBatchNotFoundException("Product batch not found"));
    }
    
    @Override
    public List<ProductBatch> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return productBatchRepository.findAll(pageable).toList();
    }
    
    @Override
    public ProductBatch update(Long productBatchId, ProductBatch productBatch) {
        ProductBatch curProductBatch = read(productBatchId);
        
        if (productBatch.getQuantity() != null) {
            curProductBatch.setQuantity(productBatch.getQuantity());
        }
        if (productBatch.getUnit() != null) {
            curProductBatch.setUnit(productBatch.getUnit());
        }
        if (productBatch.getNote() != null) {
            curProductBatch.setNote(productBatch.getNote());
        }
        if (productBatch.getReceivedAt() != null) {
            curProductBatch.setReceivedAt(productBatch.getReceivedAt());
        }
        if (productBatch.getExpiredAt() != null) {
            curProductBatch.setExpiredAt(productBatch.getExpiredAt());
        }
        if (productBatch.getProviderId() != null) {
            curProductBatch.setProviderId(productBatch.getProviderId());
        }
        if (productBatch.getSubSubcategoryId() != null) {
            curProductBatch.setSubSubcategoryId(productBatch.getSubSubcategoryId());
        }
        if (productBatch.getProcessStatus() != null) {
            curProductBatch.setProcessStatus(productBatch.getProcessStatus());
        }
        
        return productBatchRepository.save(curProductBatch);
    }
    
    @Override
    public void delete(Long productBatchId) {
        ProductBatch productBatch = read(productBatchId);
        productBatchRepository.delete(productBatch);
    }
}