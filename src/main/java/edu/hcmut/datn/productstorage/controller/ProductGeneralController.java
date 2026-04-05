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

import edu.hcmut.datn.productstorage.dao.ProductGeneral;
import edu.hcmut.datn.productstorage.dto.request.ProductGeneralCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.ProductGeneralUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.ProductGeneralService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/product-general")
public class ProductGeneralController {

    private final ProductGeneralService productGeneralService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductGeneral>> create(@RequestBody ProductGeneralCreateRequest request) {
        try {
            ProductGeneral newProductGeneral = productGeneralService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create productGeneral successfully", newProductGeneral));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{productGeneralId}")
    public ResponseEntity<ApiResponse<ProductGeneral>> read(@PathVariable Long productGeneralId) {
        try {
            ProductGeneral newProductGeneral = productGeneralService.read(productGeneralId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read productGeneral successfully", newProductGeneral));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductGeneral>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<ProductGeneral> productGenerals = productGeneralService.readAll(pageNum, pageSize);

        if (productGenerals.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No productGeneral exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all productGenerals successfully", productGenerals));
    }

    @PutMapping("/{productGeneralId}")
    public ResponseEntity<ApiResponse<ProductGeneral>> update(
            @PathVariable Long productGeneralId,
            @RequestBody ProductGeneralUpdateRequest request
    ) {
        try {
            ProductGeneral newProductGeneral = productGeneralService.update(productGeneralId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update productGeneral successfully", newProductGeneral));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{productGeneralId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long productGeneralId
    ) {
        try {
            productGeneralService.delete(productGeneralId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete productGeneral successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/suitable-for-batch/{batchId}")
    public ResponseEntity<ApiResponse<List<ProductGeneral>>> getSuitableForBatch(
            @PathVariable Long batchId
    ) {
        try {
            List<ProductGeneral> suitableProducts = productGeneralService.getSuitableForBatch(batchId);

            if (suitableProducts.isEmpty()) {
                return ResponseEntity.ok()
                        .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No suitable product general found for this batch", null));
            }

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get suitable product generals successfully", suitableProducts));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
