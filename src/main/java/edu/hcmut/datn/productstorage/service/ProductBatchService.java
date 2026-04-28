package edu.hcmut.datn.productstorage.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.productstorage.dao.ProductBatch;

public interface ProductBatchService {

    ProductBatch create(ProductBatch fridge);

    ProductBatch read(Long fridgeId);

    List<ProductBatch> readAll(Integer pageNum, Integer pageSize);

    ProductBatch update(Long fridgeId, ProductBatch fridge);

    void delete(Long fridgeId);

    ProductBatch uploadProofImages(Long batchId, List<MultipartFile> images);

    List<String> getProofImages(Long batchId);
}
