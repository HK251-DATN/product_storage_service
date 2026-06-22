package edu.hcmut.datn.productstorage.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;

public interface ProductSubBatchService {
    ProductSubBatch create(ProductSubBatch productSubBatch);
    ProductSubBatch read(Long subBatchId);
    List<ProductSubBatch> readAll(Integer pageNum, Integer pageSize);
    ProductSubBatch update(Long subBatchId, ProductSubBatch productSubBatch);
    void delete(Long subBatchId);
    ProductSubBatch uploadProofImages(Long subBatchId, List<MultipartFile> images);
    List<String> getProofImages(Long subBatchId);
    List<ProductSubBatch> findByProductBatchId(Long productBatchId);
    List<ProductSubBatch> findByProductBatchId(Long productBatchId, List<ProductBatchProcessStatus> statuses);
    ProductSubBatch acceptDelivery(Long subBatchId, Long actualQuantity, String note);
    ProductSubBatch rejectDelivery(Long subBatchId, String note);
}
