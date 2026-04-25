package edu.hcmut.datn.productstorage.controller;

import java.util.List;

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

import edu.hcmut.datn.productstorage.dao.ProductBatch;
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
}
