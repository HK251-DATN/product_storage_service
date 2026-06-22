package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import edu.hcmut.datn.productstorage.exception.ProductSubBatchAlreadyProcessedException;
import edu.hcmut.datn.productstorage.exception.ProductSubBatchNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProductSubBatchRepository;
import edu.hcmut.datn.productstorage.repository.RawProductDemandRepository;
import edu.hcmut.datn.productstorage.service.ProductSubBatchService;
import edu.hcmut.datn.productstorage.service.R2UploadService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductSubBatchServiceImpl implements ProductSubBatchService {

    private final ProductSubBatchRepository productSubBatchRepository;
    private final ProductBatchRepository productBatchRepository;
    private final RawProductDemandRepository rawProductDemandRepository;
    private final R2UploadService r2UploadService;

    @Value("${app.product-batch-proof-img-bucket}")
    private String proofImgBucket;

    @Override
    @Transactional
    public ProductSubBatch create(ProductSubBatch productSubBatch) {
        if (productSubBatch.getProviderId() != null
                && productSubBatch.getRawProductDemandId() != null
                && productSubBatch.getProductBatchId() != null) {
            Optional<ProductSubBatch> existing = productSubBatchRepository
                    .findByProviderIdAndRawProductDemandIdAndProductBatchIdAndProcessStatusIn(
                            productSubBatch.getProviderId(),
                            productSubBatch.getRawProductDemandId(),
                            productSubBatch.getProductBatchId(),
                            List.of(ProductBatchProcessStatus.WAIT_FOR_DELIVERY, ProductBatchProcessStatus.PENDING));
            if (existing.isPresent()) {
                ProductSubBatch merged = existing.get();
                long addedQty = productSubBatch.getQuantity() != null ? productSubBatch.getQuantity() : 0L;
                merged.setQuantity((merged.getQuantity() != null ? merged.getQuantity() : 0L) + addedQty);

                // For PENDING sub-batches the accept step has already run, so demand progress
                // must be updated immediately for the newly merged quantity.
                if (merged.getProcessStatus() == ProductBatchProcessStatus.PENDING && addedQty != 0) {
                    adjustDemandProgress(productSubBatch.getRawProductDemandId(), addedQty);
                }
                // For WAIT_FOR_DELIVERY, demand progress is updated at acceptDelivery time
                // using (actualQuantity - storedQuantity), so no adjustment is needed here.

                return productSubBatchRepository.save(merged);
            }
        }
        return productSubBatchRepository.save(productSubBatch);
    }

    @Override
    public ProductSubBatch read(Long subBatchId) {
        return productSubBatchRepository.findById(subBatchId)
                .orElseThrow(() -> new ProductSubBatchNotFoundException("Product sub batch not found"));
    }

    @Override
    public List<ProductSubBatch> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return productSubBatchRepository.findAll(pageable).toList();
    }

    @Override
    public ProductSubBatch update(Long subBatchId, ProductSubBatch productSubBatch) {
        ProductSubBatch curProductSubBatch = read(subBatchId);
        if (productSubBatch.getQuantity() != null) curProductSubBatch.setQuantity(productSubBatch.getQuantity());
        if (productSubBatch.getUnit() != null) curProductSubBatch.setUnit(productSubBatch.getUnit());
        if (productSubBatch.getNote() != null) curProductSubBatch.setNote(productSubBatch.getNote());
        if (productSubBatch.getReceivedAt() != null) curProductSubBatch.setReceivedAt(productSubBatch.getReceivedAt());
        if (productSubBatch.getExpiredAt() != null) curProductSubBatch.setExpiredAt(productSubBatch.getExpiredAt());
        if (productSubBatch.getProviderId() != null) curProductSubBatch.setProviderId(productSubBatch.getProviderId());
        if (productSubBatch.getSubSubcategoryId() != null) curProductSubBatch.setSubSubcategoryId(productSubBatch.getSubSubcategoryId());
        if (productSubBatch.getProductBatchId() != null) curProductSubBatch.setProductBatchId(productSubBatch.getProductBatchId());
        if (productSubBatch.getProcessStatus() != null) curProductSubBatch.setProcessStatus(productSubBatch.getProcessStatus());
        return productSubBatchRepository.save(curProductSubBatch);
    }

    @Override
    public void delete(Long subBatchId) {
        ProductSubBatch productSubBatch = read(subBatchId);
        productSubBatchRepository.delete(productSubBatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getProofImages(Long subBatchId) {
        ProductSubBatch productSubBatch = read(subBatchId);
        return List.copyOf(productSubBatch.getProofImageUrls());
    }

    @Override
    @Transactional
    public ProductSubBatch uploadProofImages(Long subBatchId, List<MultipartFile> images) {
        ProductSubBatch productSubBatch = read(subBatchId);
        List<String> uploadedUrls = images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> r2UploadService.upload(f, proofImgBucket))
                .toList();
        productSubBatch.getProofImageUrls().addAll(uploadedUrls);
        return productSubBatchRepository.save(productSubBatch);
    }

    @Override
    public List<ProductSubBatch> findByProductBatchId(Long productBatchId) {
        return productSubBatchRepository.findByProductBatchId(productBatchId);
    }

    @Override
    public List<ProductSubBatch> findByProductBatchId(Long productBatchId, List<ProductBatchProcessStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return productSubBatchRepository.findByProductBatchId(productBatchId);
        }
        return productSubBatchRepository.findByProductBatchIdAndProcessStatusIn(productBatchId, statuses);
    }

    @Override
    @Transactional
    public ProductSubBatch acceptDelivery(Long subBatchId, Long actualQuantity, String note) {
        ProductSubBatch subBatch = read(subBatchId);

        if (subBatch.getProcessStatus() != ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
            throw new ProductSubBatchAlreadyProcessedException(
                    String.format("Sub-batch is in status %s, cannot accept delivery. Expected WAIT_FOR_DELIVERY", subBatch.getProcessStatus())
            );
        }

        long quantityDiff = actualQuantity - subBatch.getQuantity();
        subBatch.setQuantity(actualQuantity);
        subBatch.setProcessStatus(ProductBatchProcessStatus.PENDING);
        if (note != null && !note.isBlank()) {
            subBatch.setNote(note);
        }

        if (subBatch.getProductBatchId() != null) {
            ProductBatch parentBatch = productBatchRepository.findById(subBatch.getProductBatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent product batch not found"));
            parentBatch.setQuantity(parentBatch.getQuantity() + quantityDiff);
            if (parentBatch.getProcessStatus() == ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
                parentBatch.setProcessStatus(ProductBatchProcessStatus.PENDING);
            }
            productBatchRepository.save(parentBatch);
        }

        if (subBatch.getRawProductDemandId() != null && quantityDiff != 0) {
            adjustDemandProgress(subBatch.getRawProductDemandId(), quantityDiff);
        }

        return productSubBatchRepository.save(subBatch);
    }

    @Override
    @Transactional
    public ProductSubBatch rejectDelivery(Long subBatchId, String note) {
        ProductSubBatch subBatch = read(subBatchId);

        if (subBatch.getProcessStatus() != ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
            throw new ProductSubBatchAlreadyProcessedException(
                    String.format("Sub-batch is in status %s, cannot reject delivery. Expected WAIT_FOR_DELIVERY", subBatch.getProcessStatus())
            );
        }

        subBatch.setProcessStatus(ProductBatchProcessStatus.REJECTED);
        if (note != null && !note.isBlank()) {
            subBatch.setNote(note);
        }

        if (subBatch.getProductBatchId() != null) {
            ProductBatch parentBatch = productBatchRepository.findById(subBatch.getProductBatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent product batch not found"));
            parentBatch.setQuantity(Math.max(0, parentBatch.getQuantity() - subBatch.getQuantity()));
            if (parentBatch.getProcessStatus() == ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
                boolean allRejected = productSubBatchRepository.findByProductBatchId(parentBatch.getBatchId())
                        .stream()
                        .allMatch(sb -> sb.getProcessStatus() == ProductBatchProcessStatus.REJECTED);
                if (allRejected) {
                    parentBatch.setProcessStatus(ProductBatchProcessStatus.REJECTED);
                }
            }
            productBatchRepository.save(parentBatch);
        }

        if (subBatch.getRawProductDemandId() != null) {
            adjustDemandProgress(subBatch.getRawProductDemandId(), -subBatch.getQuantity());
        }

        return productSubBatchRepository.save(subBatch);
    }

    private void adjustDemandProgress(Long demandId, Long delta) {
        RawProductDemand demand = rawProductDemandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("Raw product demand not found: " + demandId));

        long newProgress = Math.max(0, demand.getCurrentProgress() + delta);
        demand.setCurrentProgress(newProgress);

        if (newProgress <= 0) {
            demand.setStatus(RawProductDemandStatus.PENDING);
        } else if (newProgress >= demand.getUnitQuantity()) {
            demand.setStatus(RawProductDemandStatus.FULFILLED);
        } else {
            demand.setStatus(RawProductDemandStatus.PARTIALLY_FULFILLED);
        }

        rawProductDemandRepository.save(demand);
    }
}
