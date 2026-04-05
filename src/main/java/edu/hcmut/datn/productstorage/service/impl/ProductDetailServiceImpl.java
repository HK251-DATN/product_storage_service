package edu.hcmut.datn.productstorage.service.impl;

import java.util.ArrayList;
import java.util.List;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.exception.ProductBatchAlreadyProcessedException;
import edu.hcmut.datn.productstorage.exception.ProductBatchExpiredException;
import edu.hcmut.datn.productstorage.exception.SubSubcategoryMismatchException;
import edu.hcmut.datn.productstorage.messaging.batchdetail.BatchDetailCreateEvent;
import edu.hcmut.datn.productstorage.messaging.batchdetail.BatchDetailProducer;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import edu.hcmut.datn.productstorage.service.ProductGeneralService;
import edu.hcmut.datn.productstorage.util.UnitConverter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.productstorage.dao.ProductDetail;
import edu.hcmut.datn.productstorage.exception.ProductDetailNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductDetailRepository;
import edu.hcmut.datn.productstorage.service.ProductDetailService;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class ProductDetailServiceImpl implements ProductDetailService {
    
    private final ProductDetailRepository productDetailRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductBatchService productBatchService;
    private final ProductGeneralService productGeneralService;
    private final BatchDetailProducer batchDetailProducer;
    
    @Override
    public ProductDetail create(ProductDetail productDetail) {
        return productDetailRepository.save(productDetail);
    }
    
    @Override
    public ProductDetail read(Long productDetailId) {
        return productDetailRepository.findById(productDetailId)
                .orElseThrow(() -> new ProductDetailNotFoundException("Product detail not found"));
    }
    
    @Override
    public List<ProductDetail> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return productDetailRepository.findAll(pageable).toList();
    }
    
    @Override
    public ProductDetail update(Long productDetailId, ProductDetail productDetail) {
        ProductDetail curProductDetail = read(productDetailId);
        
        if (productDetail.getStatus() != null) {
            curProductDetail.setStatus(productDetail.getStatus());
        }
        if (productDetail.getPrice() != null) {
            curProductDetail.setPrice(productDetail.getPrice());
        }
        if (productDetail.getNumOfStar() != null) {
            curProductDetail.setNumOfStar(productDetail.getNumOfStar());
        }
        if (productDetail.getStorageToolId() != null) {
            curProductDetail.setStorageToolId(productDetail.getStorageToolId());
        }
        if (productDetail.getBatchId() != null) {
            curProductDetail.setBatchId(productDetail.getBatchId());
        }
        if (productDetail.getProdGenId() != null) {
            curProductDetail.setProdGenId(productDetail.getProdGenId());
        }
        
        return productDetailRepository.save(curProductDetail);
    }
    
    @Override
    public void delete(Long productDetailId) {
        ProductDetail productDetail = read(productDetailId);
        productDetailRepository.delete(productDetail);
    }
    
    @Override
    @Transactional
    public List<ProductDetail> processProductBatch(ProductDetail productDetail) {
        // Fetch ProductBatch and ProductGeneral
        ProductBatch productBatch = productBatchService.read(productDetail.getBatchId());
        ProductGeneral productGeneral = productGeneralService.read(productDetail.getProdGenId());

        // Validation: Check processStatus
        if (productBatch.getProcessStatus() != ProductBatchProcessStatus.PENDING) {
            throw new ProductBatchAlreadyProcessedException(
                    String.format("Product batch is already %s", productBatch.getProcessStatus())
            );
        }

        // Validation: Check expiration
        if (productBatch.getExpiredAt().isBefore(LocalDateTime.now())) {
            productBatch.setProcessStatus(ProductBatchProcessStatus.EXPIRED);
            productBatchRepository.save(productBatch);
            throw new ProductBatchExpiredException("Product batch has expired");
        }
        
        // Validation: Check if subSubcategoryId matches
        if (!productBatch.getSubSubcategoryId().equals(productGeneral.getSubSubcategoryId())) {
            throw new SubSubcategoryMismatchException(
                    String.format("SubSubcategory mismatch: ProductBatch has subSubcategoryId=%d but ProductGeneral has subSubcategoryId=%d",
                            productBatch.getSubSubcategoryId(),
                            productGeneral.getSubSubcategoryId())
            );
        }
        
        // Get unit and unitQuantity from ProductGeneral
        long numOfProdDetail = UnitConverter.splitBatch(
                productBatch.getQuantity(),
                productBatch.getUnit(),
                productGeneral.getUnitQuantity(),
                productGeneral.getUnit()
        );
        
        // Create product details
        ArrayList<ProductDetail> productDetails = new ArrayList<>();
        for (long i = 0; i < numOfProdDetail; i++) {
            productDetails.add(productDetail.copy());
        }
        
        // Save all product details
        List<ProductDetail> savedProductDetails = productDetailRepository.saveAll(productDetails);
        productDetailRepository.flush();

        // Update ProductBatch status to PROCESSED
        productBatch.setProcessStatus(ProductBatchProcessStatus.PROCESSED);
        productBatchRepository.save(productBatch);
        
        // Publish event to ecommerce
        BatchDetailCreateEvent event = new BatchDetailCreateEvent(
                (long) (Math.random() * 9999),
                productGeneral.getProdGenId(),
                numOfProdDetail,
                productDetail.getPrice(),
                (long) 0,
                (long) 0,
                ""
        );
        
        batchDetailProducer.publishBatchDetailCreated(event);
        
        return savedProductDetails;
    }
}