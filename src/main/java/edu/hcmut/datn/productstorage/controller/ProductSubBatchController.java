package edu.hcmut.datn.productstorage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.dao.ProductSubBatch;
import edu.hcmut.datn.productstorage.dto.request.DeliveryAcceptanceRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductSubBatchCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductSubBatchUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.dto.response.ProductSubBatchResponse;
import edu.hcmut.datn.productstorage.service.ProductSubBatchService;
import edu.hcmut.datn.productstorage.service.ProviderService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/product-sub-batch")
public class ProductSubBatchController {

    private final ProductSubBatchService productSubBatchService;
    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSubBatch>> create(@RequestBody ProductSubBatchCreateRequest request) {
        try {
            ProductSubBatch created = productSubBatchService.create(request.toEntity());
            return ResponseEntity.ok(ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Product sub batch created", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{subBatchId}")
    public ResponseEntity<ApiResponse<ProductSubBatch>> read(@PathVariable Long subBatchId) {
        try {
            ProductSubBatch productSubBatch = productSubBatchService.read(subBatchId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Product sub batch retrieved", productSubBatch));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSubBatch>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<ProductSubBatch> productSubBatchs = productSubBatchService.readAll(pageNum, pageSize);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Product sub batches retrieved", productSubBatchs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @PutMapping("/{subBatchId}")
    public ResponseEntity<ApiResponse<ProductSubBatch>> update(
            @PathVariable Long subBatchId,
            @RequestBody ProductSubBatchUpdateRequest request) {
        try {
            ProductSubBatch updated = productSubBatchService.update(subBatchId, request.toEntity());
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Product sub batch updated", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{subBatchId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long subBatchId) {
        try {
            productSubBatchService.delete(subBatchId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Product sub batch deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{subBatchId}/proof-images")
    public ResponseEntity<ApiResponse<List<String>>> getProofImages(@PathVariable Long subBatchId) {
        try {
            List<String> images = productSubBatchService.getProofImages(subBatchId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Proof images retrieved", images));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @PostMapping(value = "/{subBatchId}/proof-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductSubBatch>> uploadProofImages(
            @PathVariable Long subBatchId,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            ProductSubBatch updated = productSubBatchService.uploadProofImages(subBatchId, images);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Proof images uploaded", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/by-batch/{batchId}")
    public ResponseEntity<ApiResponse<List<ProductSubBatchResponse>>> findByProductBatchId(
            @PathVariable Long batchId,
            @RequestParam(required = false) List<ProductBatchProcessStatus> status) {
        try {
            List<ProductSubBatchResponse> result = productSubBatchService.findByProductBatchId(batchId, status).stream()
                    .map(subBatch -> {
                        if (subBatch.getProviderId() == null) return new ProductSubBatchResponse(subBatch, null);
                        try {
                            return new ProductSubBatchResponse(subBatch, providerService.read(subBatch.getProviderId()));
                        } catch (Exception e) {
                            return new ProductSubBatchResponse(subBatch, null);
                        }
                    })
                    .toList();
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Sub batches retrieved for batch", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/{subBatchId}/accept-delivery")
    public ResponseEntity<ApiResponse<ProductSubBatch>> acceptDelivery(
            @PathVariable Long subBatchId,
            @RequestBody DeliveryAcceptanceRequest request
    ) {
        try {
            if (request.getActualQuantity() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), "actualQuantity is required", null));
            }
            ProductSubBatch accepted = productSubBatchService.acceptDelivery(subBatchId, request.getActualQuantity(), request.getNote());
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Delivery accepted", accepted));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/{subBatchId}/reject-delivery")
    public ResponseEntity<ApiResponse<ProductSubBatch>> rejectDelivery(
            @PathVariable Long subBatchId,
            @RequestBody DeliveryAcceptanceRequest request
    ) {
        try {
            ProductSubBatch rejected = productSubBatchService.rejectDelivery(subBatchId, request.getNote());
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Delivery rejected", rejected));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
