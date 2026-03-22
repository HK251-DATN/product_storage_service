package edu.hcmut.datn.productstorage.controller;

import java.util.List;

import edu.hcmut.datn.productstorage.dto.request.ProcessProductBatchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.hcmut.datn.productstorage.dao.ProductDetail;
import edu.hcmut.datn.productstorage.dto.request.ProductDetailCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductDetailUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.ProductDetailService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/product-detail")
@Slf4j
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetail>> create(@RequestBody ProductDetailCreateRequest request) {
        try {
            ProductDetail newProductDetail = productDetailService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create productDetail successfully", newProductDetail));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{productDetailId}")
    public ResponseEntity<ApiResponse<ProductDetail>> read(@PathVariable Long productDetailId) {
        try {
            ProductDetail newProductDetail = productDetailService.read(productDetailId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read productDetail successfully", newProductDetail));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDetail>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<ProductDetail> productDetails = productDetailService.readAll(pageNum, pageSize);

        if (productDetails.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No productDetail exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all productDetails successfully", productDetails));
    }

    @PutMapping("/{productDetailId}")
    public ResponseEntity<ApiResponse<ProductDetail>> update(
            @PathVariable Long productDetailId,
            @RequestBody ProductDetailUpdateRequest request
    ) {
        try {
            ProductDetail newProductDetail = productDetailService.update(productDetailId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update productDetail successfully", newProductDetail));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productDetailId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productDetailId
    ) {
        try {
            productDetailService.delete(productDetailId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete productDetail successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/process-batch")
    public ResponseEntity<ApiResponse<List<ProductDetail>>> processBatch(
            @RequestBody ProcessProductBatchRequest request
            ) {
        try {
            List<ProductDetail> newProductDetails = productDetailService.processProductBatch(
                    request.toEntity()
            );

            newProductDetails.forEach(productDetail -> {
                log.info(productDetail.getProdDetailId().toString());
            });

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Process batch successfully", newProductDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
