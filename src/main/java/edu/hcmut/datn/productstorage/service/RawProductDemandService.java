package edu.hcmut.datn.productstorage.service;

import java.time.LocalDate;
import java.util.List;

import edu.hcmut.datn.productstorage.common.enums.TransactionStatus;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import edu.hcmut.datn.productstorage.dto.response.PagedResponse;
import edu.hcmut.datn.productstorage.dto.response.TransactionHistoryResponse;

public interface RawProductDemandService {
    RawProductDemand create(RawProductDemand demand);
    RawProductDemand read(Long demandId);
    List<RawProductDemand> readAll(Integer pageNum, Integer pageSize);
    RawProductDemand update(Long demandId, RawProductDemand demand);
    void delete(Long demandId);
    List<RawProductDemand> findByStatus(edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus status);
    List<RawProductDemand> findBySubSubcategoryId(Long subSubcategoryId);
    RawProductDemand confirmForProvider(Long demandId, Long providerId, Long quantity, String note);
    PagedResponse<TransactionHistoryResponse> getTransactionHistory(
            Long providerId,
            TransactionStatus status,
            Long subSubcategoryId,
            Long demandId,
            LocalDate dateNeedFrom,
            LocalDate dateNeedTo,
            String sortBy,
            String sortDir,
            Integer pageNum,
            Integer pageSize);
}
