package edu.hcmut.datn.productstorage.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.common.enums.TransactionStatus;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import edu.hcmut.datn.productstorage.dao.Provider;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import edu.hcmut.datn.productstorage.dao.SubSubcategory;
import edu.hcmut.datn.productstorage.dto.response.PagedResponse;
import edu.hcmut.datn.productstorage.dto.response.TransactionHistoryResponse;
import edu.hcmut.datn.productstorage.exception.RawProductDemandNotFoundException;
import edu.hcmut.datn.productstorage.repository.ProductBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProductSubBatchRepository;
import edu.hcmut.datn.productstorage.repository.ProviderRepository;
import edu.hcmut.datn.productstorage.repository.SubSubcategoryRepository;
import edu.hcmut.datn.productstorage.service.ProductSubBatchService;
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
    private final ProductSubBatchService productSubBatchService;
    private final ProviderRepository providerRepository;
    private final SubSubcategoryService subSubcategoryService;
    private final SubSubcategoryRepository subSubcategoryRepository;

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
        productSubBatchService.create(subBatch);

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

    @Override
    public PagedResponse<TransactionHistoryResponse> getTransactionHistory(
            Long providerId,
            TransactionStatus status,
            Long subSubcategoryId,
            Long demandId,
            LocalDate dateNeedFrom,
            LocalDate dateNeedTo,
            String sortBy,
            String sortDir,
            Integer pageNum,
            Integer pageSize) {

        List<ProductBatch> certBatches = productBatchRepository
                .findByRawProductDemandIdNotNullAndVerificationType(ProviderVerificationType.CERTIFICATE);
        List<ProductSubBatch> videoSubBatches = productSubBatchRepository.findByRawProductDemandIdNotNull();

        Set<Long> demandIds = new HashSet<>();
        certBatches.forEach(b -> demandIds.add(b.getRawProductDemandId()));
        videoSubBatches.forEach(s -> demandIds.add(s.getRawProductDemandId()));

        Map<Long, RawProductDemand> demandMap = rawProductDemandRepository.findAllById(demandIds)
                .stream().collect(Collectors.toMap(RawProductDemand::getDemandId, d -> d));

        Set<Long> subSubcategoryIds = demandMap.values().stream()
                .map(RawProductDemand::getSubSubcategoryId).collect(Collectors.toSet());
        Map<Long, SubSubcategory> categoryMap = subSubcategoryRepository.findAllById(subSubcategoryIds)
                .stream().collect(Collectors.toMap(SubSubcategory::getSubSubcategoryId, c -> c));

        List<TransactionHistoryResponse> responses = new ArrayList<>();

        for (ProductBatch batch : certBatches) {
            RawProductDemand demand = demandMap.get(batch.getRawProductDemandId());
            if (demand == null) continue;
            SubSubcategory category = categoryMap.get(demand.getSubSubcategoryId());
            responses.add(TransactionHistoryResponse.builder()
                    .id(batch.getBatchId())
                    .batchType(ProviderVerificationType.CERTIFICATE)
                    .demandId(batch.getRawProductDemandId())
                    .subSubcategoryId(demand.getSubSubcategoryId())
                    .subSubcategoryName(category != null ? category.getName() : null)
                    .quantity(batch.getQuantity())
                    .unit(batch.getUnit())
                    .dateNeed(demand.getDateNeed())
                    .unitPrice(demand.getUnitPrice())
                    .status(mapToTransactionStatus(batch.getProcessStatus()))
                    .providerId(batch.getProviderId())
                    .receivedAt(batch.getReceivedAt())
                    .createdAt(batch.getCreatedAt())
                    .build());
        }

        for (ProductSubBatch subBatch : videoSubBatches) {
            RawProductDemand demand = demandMap.get(subBatch.getRawProductDemandId());
            if (demand == null) continue;
            SubSubcategory category = categoryMap.get(demand.getSubSubcategoryId());
            responses.add(TransactionHistoryResponse.builder()
                    .id(subBatch.getSubBatchId())
                    .batchType(ProviderVerificationType.VIDEO)
                    .demandId(subBatch.getRawProductDemandId())
                    .subSubcategoryId(demand.getSubSubcategoryId())
                    .subSubcategoryName(category != null ? category.getName() : null)
                    .quantity(subBatch.getQuantity())
                    .unit(subBatch.getUnit())
                    .dateNeed(demand.getDateNeed())
                    .unitPrice(demand.getUnitPrice())
                    .status(mapToTransactionStatus(subBatch.getProcessStatus()))
                    .providerId(subBatch.getProviderId())
                    .receivedAt(subBatch.getReceivedAt())
                    .createdAt(subBatch.getCreatedAt())
                    .build());
        }

        Stream<TransactionHistoryResponse> stream = responses.stream();
        if (providerId != null) stream = stream.filter(r -> providerId.equals(r.getProviderId()));
        if (status != null) stream = stream.filter(r -> status == r.getStatus());
        if (subSubcategoryId != null) stream = stream.filter(r -> subSubcategoryId.equals(r.getSubSubcategoryId()));
        if (demandId != null) stream = stream.filter(r -> demandId.equals(r.getDemandId()));
        if (dateNeedFrom != null) stream = stream.filter(r -> r.getDateNeed() != null && !r.getDateNeed().isBefore(dateNeedFrom));
        if (dateNeedTo != null) stream = stream.filter(r -> r.getDateNeed() != null && !r.getDateNeed().isAfter(dateNeedTo));

        List<TransactionHistoryResponse> filtered = stream.collect(Collectors.toList());
        filtered.sort(buildComparator(sortBy, sortDir));

        int total = filtered.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<TransactionHistoryResponse> page = fromIndex >= total ? List.of() : filtered.subList(fromIndex, toIndex);
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;

        return PagedResponse.<TransactionHistoryResponse>builder()
                .data(page)
                .page(pageNum)
                .size(pageSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private TransactionStatus mapToTransactionStatus(ProductBatchProcessStatus processStatus) {
        return switch (processStatus) {
            case WAIT_FOR_DELIVERY -> TransactionStatus.WAIT_FOR_DELIVERY;
            case PENDING, PROCESSED -> TransactionStatus.FINISHED;
            case REJECTED -> TransactionStatus.REJECTED;
            case EXPIRED -> TransactionStatus.EXPIRED;
        };
    }

    private Comparator<TransactionHistoryResponse> buildComparator(String sortBy, String sortDir) {
        Comparator<TransactionHistoryResponse> comparator = switch (sortBy != null ? sortBy : "createdAt") {
            case "dateNeed" -> Comparator.comparing(
                    r -> r.getDateNeed() != null ? r.getDateNeed() : LocalDate.MIN);
            case "quantity" -> Comparator.comparingLong(
                    r -> r.getQuantity() != null ? r.getQuantity() : 0L);
            case "unitPrice" -> Comparator.comparingLong(
                    r -> r.getUnitPrice() != null ? r.getUnitPrice() : 0L);
            case "receivedAt" -> Comparator.comparing(
                    r -> r.getReceivedAt() != null ? r.getReceivedAt() : LocalDateTime.MIN);
            default -> Comparator.comparing(
                    r -> r.getCreatedAt() != null ? r.getCreatedAt() : LocalDateTime.MIN);
        };
        return "ASC".equalsIgnoreCase(sortDir) ? comparator : comparator.reversed();
    }
}
