package edu.hcmut.datn.productstorage.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import edu.hcmut.datn.productstorage.dao.Provider;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import edu.hcmut.datn.productstorage.dao.SubSubcategory;
import edu.hcmut.datn.productstorage.exception.ProductBatchNotFoundException;
import edu.hcmut.datn.productstorage.exception.RawProductDemandNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProductSubBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProviderRepository;
import edu.hcmut.datn.productstorage.repository.RawProductDemandRepository;
import edu.hcmut.datn.productstorage.service.RawProductDemandService;
import edu.hcmut.datn.productstorage.service.SubSubcategoryService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RawProductDemandServiceImpl implements RawProductDemandService {

    private final RawProductDemandRepository rawProductDemandRepository;
    private final ProductBatchRepository productBatchRepository;
    private final ProductSubBatchRepository productSubBatchRepository;
    private final ProviderRepository providerRepository;
    private final SubSubcategoryService subSubcategoryService;

    @Override
    public RawProductDemand create(RawProductDemand demand) {
        return rawProductDemandRepository.save(demand);
    }

    @Override
    public RawProductDemand read(Long demandId) {
        return rawProductDemandRepository.findById(demandId)
                .orElseThrow(() -> new RawProductDemandNotFoundException("Raw product demand not found"));
    }

    @Override
    public List<RawProductDemand> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return rawProductDemandRepository.findAll(pageable).toList();
    }

    @Override
    public RawProductDemand update(Long demandId, RawProductDemand demand) {
        RawProductDemand curDemand = read(demandId);
        if (demand.getSubSubcategoryId() != null) curDemand.setSubSubcategoryId(demand.getSubSubcategoryId());
        if (demand.getUnit() != null) curDemand.setUnit(demand.getUnit());
        if (demand.getUnitQuantity() != null) curDemand.setUnitQuantity(demand.getUnitQuantity());
        if (demand.getUnitPrice() != null) curDemand.setUnitPrice(demand.getUnitPrice());
        if (demand.getDateNeed() != null) curDemand.setDateNeed(demand.getDateNeed());
        if (demand.getNote() != null) curDemand.setNote(demand.getNote());
        if (demand.getStatus() != null) curDemand.setStatus(demand.getStatus());
        return rawProductDemandRepository.save(curDemand);
    }

    @Override
    public void delete(Long demandId) {
        RawProductDemand demand = read(demandId);
        rawProductDemandRepository.delete(demand);
    }

    @Override
    public List<RawProductDemand> findByStatus(RawProductDemandStatus status) {
        return rawProductDemandRepository.findByStatus(status);
    }

    @Override
    public List<RawProductDemand> findBySubSubcategoryId(Long subSubcategoryId) {
        return rawProductDemandRepository.findBySubSubcategoryId(subSubcategoryId);
    }

    @Override
    @Transactional
    public RawProductDemand confirmForProvider(Long demandId, Long providerId, Long quantity, String note) {
        RawProductDemand demand = read(demandId);

        long remainingQuantity = demand.getUnitQuantity() - demand.getCurrentProgress();
        if (quantity > remainingQuantity) {
            throw new IllegalArgumentException(
                    String.format("Requested quantity %d exceeds remaining demand %d", quantity, remainingQuantity)
            );
        }

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found with id: " + providerId));

        if (provider.getVerificationMethod() == ProviderVerificationType.CERTIFICATE) {
            handleCertificateProvider(demand, providerId, quantity, note);
        } else {
            handleVideoProvider(demand, providerId, quantity, note);
        }

        demand.setCurrentProgress(demand.getCurrentProgress() + quantity);
        if (demand.getCurrentProgress() >= demand.getUnitQuantity()) {
            demand.setStatus(RawProductDemandStatus.FULFILLED);
        } else {
            demand.setStatus(RawProductDemandStatus.PARTIALLY_FULFILLED);
        }

        return rawProductDemandRepository.save(demand);
    }

    private void handleCertificateProvider(RawProductDemand demand, Long providerId, Long quantity, String note) {
        var existingOpt = productBatchRepository.findByRawProductDemandIdAndProviderId(demand.getDemandId(), providerId);

        if (existingOpt.isPresent()) {
            ProductBatch existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + quantity);
            productBatchRepository.save(existing);
        } else {
            ProductBatch newBatch = new ProductBatch(
                    quantity,
                    demand.getUnit(),
                    note,
                    LocalDateTime.now(),
                    calculateExpiredAt(demand.getSubSubcategoryId()),
                    providerId,
                    demand.getSubSubcategoryId()
            );
            newBatch.setVerificationType(ProviderVerificationType.CERTIFICATE);
            newBatch.setRawProductDemandId(demand.getDemandId());
            newBatch.setProcessStatus(ProductBatchProcessStatus.WAIT_FOR_DELIVERY);
            productBatchRepository.save(newBatch);
        }
    }

    private void handleVideoProvider(RawProductDemand demand, Long providerId, Long quantity, String note) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<ProductBatch> sharedBatches = productBatchRepository
                .findByRawProductDemandIdAndProviderIdIsNullAndVerificationType(
                        demand.getDemandId(), ProviderVerificationType.VIDEO);

        ProductBatch sharedBatch = sharedBatches.stream()
                .filter(b -> b.getReceivedAt() != null && b.getReceivedAt().toLocalDate().isEqual(today))
                .findFirst()
                .orElse(null);

        if (sharedBatch == null) {
            sharedBatch = new ProductBatch(
                    0L,
                    demand.getUnit(),
                    note,
                    now,
                    calculateExpiredAt(demand.getSubSubcategoryId()),
                    null,
                    demand.getSubSubcategoryId()
            );
            sharedBatch.setVerificationType(ProviderVerificationType.VIDEO);
            sharedBatch.setRawProductDemandId(demand.getDemandId());
            sharedBatch.setProcessStatus(ProductBatchProcessStatus.WAIT_FOR_DELIVERY);
            sharedBatch = productBatchRepository.save(sharedBatch);
        }

        ProductSubBatch subBatch = new ProductSubBatch(
                quantity,
                demand.getUnit(),
                note,
                now,
                sharedBatch.getExpiredAt(),
                providerId,
                demand.getSubSubcategoryId(),
                sharedBatch.getBatchId(),
                demand.getDemandId()
        );
        subBatch.setProcessStatus(ProductBatchProcessStatus.WAIT_FOR_DELIVERY);
        productSubBatchRepository.save(subBatch);

        sharedBatch.setQuantity(sharedBatch.getQuantity() + quantity);
        productBatchRepository.save(sharedBatch);
    }

    private LocalDateTime calculateExpiredAt(Long subSubcategoryId) {
        SubSubcategory subSubcategory = subSubcategoryService.read(subSubcategoryId);
        if (subSubcategory.getAvgShelfDays() != null) {
            return LocalDateTime.now().plusDays(subSubcategory.getAvgShelfDays());
        }
        return LocalDateTime.now().plusDays(3);
    }
}
