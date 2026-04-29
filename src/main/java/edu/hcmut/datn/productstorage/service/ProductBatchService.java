package edu.hcmut.datn.productstorage.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.dao.ProductBatch;

public interface ProductBatchService {

    ProductBatch create(ProductBatch productBatch);

    ProductBatch read(Long productBatchId);

    List<ProductBatch> readAll(Integer pageNum, Integer pageSize);

    ProductBatch update(Long productBatchId, ProductBatch productBatch);

    void delete(Long productBatchId);

    ProductBatch uploadProofImages(Long batchId, List<MultipartFile> images);

    List<String> getProofImages(Long batchId);

    List<ProductBatch> findByProcessStatus(ProductBatchProcessStatus processStatus);

    ProductBatch acceptDelivery(Long batchId, Long actualQuantity, String note);

    ProductBatch rejectDelivery(Long batchId, String note);
}
