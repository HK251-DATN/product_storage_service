package edu.hcmut.datn.productstorage.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import edu.hcmut.datn.productstorage.dao.ProductDetail;
import edu.hcmut.datn.productstorage.dto.request.ProcessBatchRequest;
import edu.hcmut.datn.productstorage.dto.response.ProcessBatchResponse;

public interface ProductDetailService {

    ProductDetail create(ProductDetail productDetail);

    ProductDetail read(Long productDetailId);

    List<ProductDetail> readAll(Integer pageNum, Integer pageSize);

    Page<ProductDetail> readAll(Integer pageNum, Integer pageSize,
                                Long batchId, Long prodGenId, Long subBatchId,
                                String sortBy, String sortDir);

    ProductDetail update(Long productDetailId, ProductDetail productDetail);

    void delete(Long productDetailId);

    List<ProductDetail> processProductBatch(ProductDetail productDetail);

    /**
     * Process a ProductBatch into ProductDetails.
     * Automatically handles both CERTIFICATE and VIDEO verified batches:
     * - CERTIFICATE: Creates product details with provider attribution
     * - VIDEO: Distributes product details proportionally across sub-batches
     *
     * @param request The batch processing request
     * @return Response with detailed breakdown of created product details
     */
    ProcessBatchResponse processProductBatchV2(ProcessBatchRequest request);

    Optional<Long> findProdGenIdByBatchId(Long batchId);
}
