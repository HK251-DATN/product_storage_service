package edu.hcmut.datn.productstorage.service.impl;

import java.util.List;

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
import edu.hcmut.datn.productstorage.exception.ProductBatchAlreadyProcessedException;
import edu.hcmut.datn.productstorage.exception.ProductBatchNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProductSubBatchRepository;
import edu.hcmut.datn.productstorage.repository.RawProductDemandRepository;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import edu.hcmut.datn.productstorage.service.R2UploadService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository productBatchRepository;
    private final ProductSubBatchRepository productSubBatchRepository;
    private final RawProductDemandRepository rawProductDemandRepository;
    private final R2UploadService r2UploadService;

    @Value("${app.product-batch-proof-img-bucket}")
    private String proofImgBucket;

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
        if (productBatch.getVerificationType() != null) {
            curProductBatch.setVerificationType(productBatch.getVerificationType());
        }
        if (productBatch.getRawProductDemandId() != null) {
            curProductBatch.setRawProductDemandId(productBatch.getRawProductDemandId());
        }

        return productBatchRepository.save(curProductBatch);
    }

    @Override
    public void delete(Long productBatchId) {
        ProductBatch productBatch = read(productBatchId);
        productBatchRepository.delete(productBatch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getProofImages(Long batchId) {
        ProductBatch productBatch = read(batchId);
        return List.copyOf(productBatch.getProofImageUrls());
    }

    @Override
    public ProductBatch uploadProofImages(Long batchId, List<MultipartFile> images) {
        ProductBatch productBatch = read(batchId);

        List<String> uploadedUrls = images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> r2UploadService.upload(f, proofImgBucket))
                .toList();

        productBatch.getProofImageUrls().addAll(uploadedUrls);
        return productBatchRepository.save(productBatch);
    }

    @Override
    public List<ProductBatch> findByProcessStatus(ProductBatchProcessStatus processStatus) {
        return productBatchRepository.findByProcessStatus(processStatus);
    }

    @Override
    @Transactional
    public ProductBatch acceptDelivery(Long batchId, Long actualQuantity, String note) {
        ProductBatch batch = read(batchId);

        if (batch.getProcessStatus() != ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
            throw new ProductBatchAlreadyProcessedException(
                    String.format("Batch is in status %s, cannot accept delivery. Expected WAIT_FOR_DELIVERY", batch.getProcessStatus())
            );
        }

        long quantityDiff = actualQuantity - batch.getQuantity();
        batch.setQuantity(actualQuantity);
        batch.setProcessStatus(ProductBatchProcessStatus.PENDING);
        if (note != null && !note.isBlank()) {
            batch.setNote(note);
        }

        if (batch.getRawProductDemandId() != null && quantityDiff != 0) {
            adjustDemandProgress(batch.getRawProductDemandId(), quantityDiff);
        }

        if (batch.getProviderId() == null) {
            List<ProductSubBatch> subBatches = productSubBatchRepository.findByProductBatchId(batchId);
            for (ProductSubBatch subBatch : subBatches) {
                if (subBatch.getProcessStatus() == ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
                    subBatch.setProcessStatus(ProductBatchProcessStatus.PENDING);
                    productSubBatchRepository.save(subBatch);
                }
            }
        }

        return productBatchRepository.save(batch);
    }

    @Override
    @Transactional
    public ProductBatch rejectDelivery(Long batchId, String note) {
        ProductBatch batch = read(batchId);

        if (batch.getProcessStatus() != ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
            throw new ProductBatchAlreadyProcessedException(
                    String.format("Batch is in status %s, cannot reject delivery. Expected WAIT_FOR_DELIVERY", batch.getProcessStatus())
            );
        }

        batch.setProcessStatus(ProductBatchProcessStatus.REJECTED);
        if (note != null && !note.isBlank()) {
            batch.setNote(note);
        }

        if (batch.getRawProductDemandId() != null) {
            adjustDemandProgress(batch.getRawProductDemandId(), -batch.getQuantity());
        }

        if (batch.getProviderId() == null) {
            List<ProductSubBatch> subBatches = productSubBatchRepository.findByProductBatchId(batchId);
            for (ProductSubBatch subBatch : subBatches) {
                if (subBatch.getProcessStatus() == ProductBatchProcessStatus.WAIT_FOR_DELIVERY) {
                    subBatch.setProcessStatus(ProductBatchProcessStatus.REJECTED);
                    productSubBatchRepository.save(subBatch);
                }
            }
        }

        return productBatchRepository.save(batch);
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
