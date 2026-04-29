package edu.hcmut.datn.productstorage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dto.request.DeliveryAcceptanceRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductBatchCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductBatchUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/product-batch")
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductBatch>> create(@RequestBody ProductBatchCreateRequest request) {
        try {
            ProductBatch newProductBatch = productBatchService.create(request.toEntity());
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create productBatch successfully", newProductBatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{productBatchId}")
    public ResponseEntity<ApiResponse<ProductBatch>> read(@PathVariable Long productBatchId) {
        try {
            ProductBatch newProductBatch = productBatchService.read(productBatchId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read productBatch successfully", newProductBatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductBatch>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<ProductBatch> productBatchs = productBatchService.readAll(pageNum, pageSize);
        if (productBatchs.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No productBatch exists", null));
        }
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all productBatchs successfully", productBatchs));
    }

    @PutMapping("/{productBatchId}")
    public ResponseEntity<ApiResponse<ProductBatch>> update(
            @PathVariable Long productBatchId,
            @RequestBody ProductBatchUpdateRequest request
    ) {
        try {
            ProductBatch newProductBatch = productBatchService.update(productBatchId, request.toEntity());
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update productBatch successfully", newProductBatch));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productBatchId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productBatchId
    ) {
        try {
            productBatchService.delete(productBatchId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete productBatch successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{productBatchId}/proof-images")
    public ResponseEntity<ApiResponse<List<String>>> getProofImages(
            @PathVariable Long productBatchId
    ) {
        try {
            List<String> urls = productBatchService.getProofImages(productBatchId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get proof images successfully", urls));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping(value = "/{productBatchId}/proof-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductBatch>> uploadProofImages(
            @PathVariable Long productBatchId,
            @RequestParam("images") List<MultipartFile> images
    ) {
        try {
            ProductBatch updated = productBatchService.uploadProofImages(productBatchId, images);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Upload proof images successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ProductBatch>>> findByProcessStatus(@PathVariable ProductBatchProcessStatus status) {
        try {
            List<ProductBatch> batches = productBatchService.findByProcessStatus(status);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Batches retrieved by status", batches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/{productBatchId}/accept-delivery")
    public ResponseEntity<ApiResponse<ProductBatch>> acceptDelivery(
            @PathVariable Long productBatchId,
            @RequestBody DeliveryAcceptanceRequest request
    ) {
        try {
            if (request.getActualQuantity() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "actualQuantity is required", null));
            }
            ProductBatch accepted = productBatchService.acceptDelivery(productBatchId, request.getActualQuantity(), request.getNote());
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delivery accepted", accepted));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/{productBatchId}/reject-delivery")
    public ResponseEntity<ApiResponse<ProductBatch>> rejectDelivery(
            @PathVariable Long productBatchId,
            @RequestBody DeliveryAcceptanceRequest request
    ) {
        try {
            ProductBatch rejected = productBatchService.rejectDelivery(productBatchId, request.getNote());
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delivery rejected", rejected));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
