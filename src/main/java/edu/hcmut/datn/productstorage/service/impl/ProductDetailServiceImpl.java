package edu.hcmut.datn.productstorage.service.impl;

import java.util.ArrayList;
import java.util.List;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProductStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import edu.hcmut.datn.productstorage.dto.request.ProcessBatchRequest;
import edu.hcmut.datn.productstorage.dto.response.ProcessBatchResponse;
import edu.hcmut.datn.productstorage.exception.ProductBatchAlreadyProcessedException;
import edu.hcmut.datn.productstorage.exception.ProductBatchExpiredException;
import edu.hcmut.datn.productstorage.exception.ProductSubBatchAlreadyProcessedException;
import edu.hcmut.datn.productstorage.exception.SubSubcategoryMismatchException;
import edu.hcmut.datn.productstorage.messaging.batchdetail.BatchDetailCreateEvent;
import edu.hcmut.datn.productstorage.messaging.batchdetail.BatchDetailProducer;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProductSubBatchRepository;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import edu.hcmut.datn.productstorage.service.ProductGeneralService;
import edu.hcmut.datn.productstorage.service.ProductSubBatchService;
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
    private final ProductSubBatchRepository productSubBatchRepository;
    private final ProductBatchService productBatchService;
    private final ProductSubBatchService productSubBatchService;
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
        if (productDetail.getSubBatchId() != null) {
            curProductDetail.setSubBatchId(productDetail.getSubBatchId());
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
        ProductBatch productBatch = productBatchService.read(productDetail.getBatchId());
        ProductGeneral productGeneral = productGeneralService.read(productDetail.getProdGenId());

        if (productBatch.getProcessStatus() != ProductBatchProcessStatus.PENDING) {
            throw new ProductBatchAlreadyProcessedException(
                    String.format("Product batch is already %s", productBatch.getProcessStatus())
            );
        }

        if (productBatch.getExpiredAt().isBefore(LocalDateTime.now())) {
            productBatch.setProcessStatus(ProductBatchProcessStatus.EXPIRED);
            productBatchRepository.save(productBatch);
            throw new ProductBatchExpiredException("Product batch has expired");
        }

        if (!productBatch.getSubSubcategoryId().equals(productGeneral.getSubSubcategoryId())) {
            throw new SubSubcategoryMismatchException(
                    String.format("SubSubcategory mismatch: ProductBatch has subSubcategoryId=%d but ProductGeneral has subSubcategoryId=%d",
                            productBatch.getSubSubcategoryId(),
                            productGeneral.getSubSubcategoryId())
            );
        }

        long numOfProdDetail = UnitConverter.splitBatch(
                productBatch.getQuantity(),
                productBatch.getUnit(),
                productGeneral.getUnitQuantity(),
                productGeneral.getUnit()
        );

        ArrayList<ProductDetail> productDetails = new ArrayList<>();
        for (long i = 0; i < numOfProdDetail; i++) {
            productDetails.add(productDetail.copy());
        }

        List<ProductDetail> savedProductDetails = productDetailRepository.saveAll(productDetails);
        productDetailRepository.flush();

        productBatch.setProcessStatus(ProductBatchProcessStatus.PROCESSED);
        productBatchRepository.save(productBatch);

        BatchDetailCreateEvent event = new BatchDetailCreateEvent(
                productBatch.getBatchId(),
                productGeneral.getProdGenId(),
                numOfProdDetail,
                productDetail.getPrice(),
                0L,
                0L,
                "",
                productDetail.getSubBatchId(),
                productBatch.getVerificationType() != null ? productBatch.getVerificationType().name() : null,
                productBatch.getProviderId()
        );

        batchDetailProducer.publishBatchDetailCreated(event);

        return savedProductDetails;
    }

    @Override
    @Transactional
    public ProcessBatchResponse processProductBatchV2(ProcessBatchRequest request) {
        // Fetch batch and product general
        ProductBatch productBatch = productBatchService.read(request.getBatchId());
        ProductGeneral productGeneral = productGeneralService.read(request.getProductGeneralId());

        // Validate batch status
        if (productBatch.getProcessStatus() != ProductBatchProcessStatus.PENDING) {
            throw new ProductBatchAlreadyProcessedException(
                    String.format("Product batch is already %s", productBatch.getProcessStatus())
            );
        }

        // Validate expiry
        if (productBatch.getExpiredAt().isBefore(LocalDateTime.now())) {
            productBatch.setProcessStatus(ProductBatchProcessStatus.EXPIRED);
            productBatchRepository.save(productBatch);
            throw new ProductBatchExpiredException("Product batch has expired");
        }

        // Validate category match
        if (!productBatch.getSubSubcategoryId().equals(productGeneral.getSubSubcategoryId())) {
            throw new SubSubcategoryMismatchException(
                    String.format("SubSubcategory mismatch: ProductBatch has subSubcategoryId=%d but ProductGeneral has subSubcategoryId=%d",
                            productBatch.getSubSubcategoryId(),
                            productGeneral.getSubSubcategoryId())
            );
        }

        // Determine workflow based on verification type
        ProviderVerificationType verificationType = productBatch.getVerificationType();

        if (verificationType == ProviderVerificationType.CERTIFICATE) {
            return processCertificateBatch(productBatch, productGeneral, request);
        } else {
            return processVideoBatch(productBatch, productGeneral, request);
        }
    }

    /**
     * Process CERTIFICATE-verified batch (single provider, with attribution)
     */
    private ProcessBatchResponse processCertificateBatch(
            ProductBatch productBatch,
            ProductGeneral productGeneral,
            ProcessBatchRequest request) {

        // Calculate number of product details
        long numOfProdDetail = UnitConverter.splitBatch(
                productBatch.getQuantity(),
                productBatch.getUnit(),
                productGeneral.getUnitQuantity(),
                productGeneral.getUnit()
        );

        // Create product details
        List<ProductDetail> productDetails = new ArrayList<>();
        for (long i = 0; i < numOfProdDetail; i++) {
            ProductDetail detail = new ProductDetail(
                    ProductStatus.STORED,
                    request.getPrice(),
                    request.getNumOfStar() != null ? request.getNumOfStar() : 0L,
                    request.getStorageToolId(),
                    productBatch.getBatchId(),
                    productGeneral.getProdGenId()
            );
            // For CERTIFICATE batches: sub_batch_id is null
            detail.setSubBatchId(null);
            productDetails.add(detail);
        }

        // Save product details
        List<ProductDetail> savedProductDetails = productDetailRepository.saveAll(productDetails);
        productDetailRepository.flush();

        // Update batch status
        productBatch.setProcessStatus(ProductBatchProcessStatus.PROCESSED);
        productBatchRepository.save(productBatch);

        // Publish event WITH provider information
        BatchDetailCreateEvent event = new BatchDetailCreateEvent(
                productBatch.getBatchId(),
                productGeneral.getProdGenId(),
                numOfProdDetail,
                request.getPrice(),
                0L,
                0L,
                "",
                null, // no sub-batch for CERTIFICATE
                "VIETGAP",
                productBatch.getProviderId() // Include provider ID
        );
        batchDetailProducer.publishBatchDetailCreated(event);

        // Build response
        return new ProcessBatchResponse(
                ProviderVerificationType.CERTIFICATE,
                numOfProdDetail,
                productBatch.getBatchId(),
                productGeneral.getProdGenId(),
                productBatch.getProviderId(),
                List.of() // No sub-batch breakdown
        );
    }

    /**
     * Process VIDEO-verified batch (multiple providers, pooled, no attribution)
     */
    private ProcessBatchResponse processVideoBatch(
            ProductBatch productBatch,
            ProductGeneral productGeneral,
            ProcessBatchRequest request) {

        // Get all sub-batches for this batch
        List<ProductSubBatch> subBatches = productSubBatchService.findByProductBatchId(productBatch.getBatchId());

        if (subBatches.isEmpty()) {
            throw new IllegalStateException("VIDEO-verified batch must have at least one sub-batch");
        }

        List<ProcessBatchResponse.SubBatchBreakdown> breakdowns = new ArrayList<>();
        List<ProductDetail> allProductDetails = new ArrayList<>();
        long totalProductDetailsCreated = 0;

        // Process each sub-batch
        for (ProductSubBatch subBatch : subBatches) {
            // Validate sub-batch status
            if (subBatch.getProcessStatus() != ProductBatchProcessStatus.PENDING) {
                throw new ProductSubBatchAlreadyProcessedException(
                        String.format("ProductSubBatch %d is already %s", subBatch.getSubBatchId(), subBatch.getProcessStatus())
                );
            }

            // Validate expiry
            if (subBatch.getExpiredAt().isBefore(LocalDateTime.now())) {
                subBatch.setProcessStatus(ProductBatchProcessStatus.EXPIRED);
                productSubBatchRepository.save(subBatch);
                throw new ProductBatchExpiredException(
                        String.format("ProductSubBatch %d has expired", subBatch.getSubBatchId())
                );
            }

            // Validate category match
            if (!subBatch.getSubSubcategoryId().equals(productGeneral.getSubSubcategoryId())) {
                throw new SubSubcategoryMismatchException(
                        String.format("SubSubcategory mismatch: ProductSubBatch %d has subSubcategoryId=%d but ProductGeneral has subSubcategoryId=%d",
                                subBatch.getSubBatchId(),
                                subBatch.getSubSubcategoryId(),
                                productGeneral.getSubSubcategoryId())
                );
            }

            // Calculate number of product details for this sub-batch
            long numOfProdDetail = UnitConverter.splitBatch(
                    subBatch.getQuantity(),
                    subBatch.getUnit(),
                    productGeneral.getUnitQuantity(),
                    productGeneral.getUnit()
            );

            // Create product details for this sub-batch
            for (long i = 0; i < numOfProdDetail; i++) {
                ProductDetail detail = new ProductDetail(
                        ProductStatus.STORED,
                        request.getPrice(),
                        request.getNumOfStar() != null ? request.getNumOfStar() : 0L,
                        request.getStorageToolId(),
                        productBatch.getBatchId(), // Parent batch ID (shared)
                        productGeneral.getProdGenId()
                );
                // For VIDEO batches: set sub_batch_id to track source provider
                detail.setSubBatchId(subBatch.getSubBatchId());
                allProductDetails.add(detail);
            }

            // Update sub-batch status
            subBatch.setProcessStatus(ProductBatchProcessStatus.PROCESSED);
            productSubBatchRepository.save(subBatch);

            // Add to breakdown
            breakdowns.add(new ProcessBatchResponse.SubBatchBreakdown(
                    subBatch.getSubBatchId(),
                    subBatch.getProviderId(),
                    subBatch.getQuantity(),
                    numOfProdDetail
            ));

            totalProductDetailsCreated += numOfProdDetail;
        }

        // Save all product details
        productDetailRepository.saveAll(allProductDetails);
        productDetailRepository.flush();

        // Update parent batch status
        productBatch.setProcessStatus(ProductBatchProcessStatus.PROCESSED);
        productBatchRepository.save(productBatch);

        // Publish event WITHOUT provider information (pooled product)
        BatchDetailCreateEvent event = new BatchDetailCreateEvent(
                productBatch.getBatchId(),
                productGeneral.getProdGenId(),
                totalProductDetailsCreated,
                request.getPrice(),
                0L,
                0L,
                "",
                null, // No specific sub-batch for pooled product
                ProviderVerificationType.VIDEO.name(),
                null // No provider ID for VIDEO batches (pooled)
        );
        batchDetailProducer.publishBatchDetailCreated(event);

        // Build response
        return new ProcessBatchResponse(
                ProviderVerificationType.VIDEO,
                totalProductDetailsCreated,
                productBatch.getBatchId(),
                productGeneral.getProdGenId(),
                null, // No single provider for VIDEO batches
                breakdowns
        );
    }
}
