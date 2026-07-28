package edu.hcmut.datn.productstorage.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.ProviderVerificationType;
import edu.hcmut.datn.productstorage.dao.ProductBatch;
import edu.hcmut.datn.productstorage.dto.request.DeliveryAcceptanceRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductBatchCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductBatchUpdateRequest;
import edu.hcmut.datn.productstorage.dao.Provider;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.dto.response.PagedResponse;
import edu.hcmut.datn.productstorage.dto.response.ProductBatchDetailResponse;
import edu.hcmut.datn.productstorage.dto.response.ProductBatchResponse;
import edu.hcmut.datn.productstorage.dto.response.ProductSubBatchResponse;
import edu.hcmut.datn.productstorage.service.ProductBatchService;
import edu.hcmut.datn.productstorage.service.ProductSubBatchService;
import edu.hcmut.datn.productstorage.service.ProviderService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/product-batch")
public class ProductBatchController {

    private final ProductBatchService productBatchService;
    private final ProductSubBatchService productSubBatchService;
    private final ProviderService providerService;

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
    public ResponseEntity<ApiResponse<ProductBatchDetailResponse>> read(@PathVariable Long productBatchId) {
        try {
            ProductBatch batch = productBatchService.read(productBatchId);
            ProductBatchDetailResponse response;

            if (batch.getVerificationType() == ProviderVerificationType.VIDEO) {
                List<ProductSubBatchResponse> subBatches = productSubBatchService.findByProductBatchId(productBatchId)
                        .stream()
                        .map(subBatch -> {
                            if (subBatch.getProviderId() == null) return new ProductSubBatchResponse(subBatch, null);
                            try {
                                return new ProductSubBatchResponse(subBatch, providerService.read(subBatch.getProviderId()));
                            } catch (Exception ex) {
                                return new ProductSubBatchResponse(subBatch, null);
                            }
                        })
                        .toList();
                response = ProductBatchDetailResponse.video(batch, subBatches);
            } else {
                Provider provider = null;
                if (batch.getProviderId() != null) {
                    try { provider = providerService.read(batch.getProviderId()); } catch (Exception ignored) {}
                }
                response = ProductBatchDetailResponse.certificate(batch, provider);
            }

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read productBatch successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/product-batch
    // -------------------------------------------------------------------------
    // Query params:
    //   pageNum          (int,    default: 1)    – 1-based page number
    //   pageSize         (int,    default: 10)   – items per page
    //   processStatus    (enum,   optional)      – WAIT_FOR_DELIVERY | PENDING | PROCESSED | EXPIRED | REJECTED
    //   verificationType (enum,   optional)      – CERTIFICATE | VIDEO
    //   providerId       (Long,   optional)      – exact match on provider ID
    //   subSubcategoryId (Long,   optional)      – exact match on sub-subcategory ID
    //   sortBy           (string, default: createdAt) – createdAt | receivedAt | expiredAt | quantity | batchId
    //   sortDir          (string, default: desc)      – asc | desc
    //
    // Example request:
    //   GET /api/product-batch?pageNum=1&pageSize=10&processStatus=PENDING&sortBy=expiredAt&sortDir=asc
    //
    // Response 200 – records found (type: GOOD):
    //   {
    //     "type": "GOOD",
    //     "code": "200 OK",
    //     "message": "Get all productBatchs successfully",
    //     "detail": {
    //       "data": [ { "batchId": 1, "quantity": 50, "unit": "KILOGRAM", ... } ],
    //       "page": 1,
    //       "size": 10,
    //       "totalElements": 35,
    //       "totalPages": 4
    //     },
    //     "timestamp": "2026-05-16T10:00:00"
    //   }
    //
    // Response 200 – no records found (type: SKIP_AS_GOOD):
    //   {
    //     "type": "SKIP_AS_GOOD",
    //     "code": "200 OK",
    //     "message": "No productBatch exists",
    //     "detail": { "data": [], "page": 1, "size": 10, "totalElements": 0, "totalPages": 0 },
    //     "timestamp": "2026-05-16T10:00:00"
    //   }
    // -------------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductBatchResponse>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) ProductBatchProcessStatus processStatus,
            @RequestParam(required = false) ProviderVerificationType verificationType,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) Long subSubcategoryId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<ProductBatch> page = productBatchService.readAll(
                pageNum, pageSize, processStatus, verificationType, providerId, subSubcategoryId, sortBy, sortDir);
        Page<ProductBatchResponse> responsePage = page.map(batch -> {
            if (batch.getProviderId() == null) return new ProductBatchResponse(batch, null);
            try {
                return new ProductBatchResponse(batch, providerService.read(batch.getProviderId()));
            } catch (Exception e) {
                return new ProductBatchResponse(batch, null);
            }
        });
        PagedResponse<ProductBatchResponse> pagedResponse = PagedResponse.of(responsePage, pageNum);
        if (page.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No productBatch exists", pagedResponse));
        }
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all productBatchs successfully", pagedResponse));
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
