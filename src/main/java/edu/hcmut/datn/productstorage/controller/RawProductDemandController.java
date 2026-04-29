package edu.hcmut.datn.productstorage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import edu.hcmut.datn.productstorage.dao.RawProductDemand;
import edu.hcmut.datn.productstorage.dto.request.RawProductDemandConfirmRequest;
import edu.hcmut.datn.productstorage.dto.request.RawProductDemandCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.RawProductDemandUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.security.portable.AuthenticatedUser;
import edu.hcmut.datn.productstorage.service.RawProductDemandService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/raw-product-demand")
public class RawProductDemandController {

    private final RawProductDemandService rawProductDemandService;

    @PostMapping
    public ResponseEntity<ApiResponse<RawProductDemand>> create(@RequestBody RawProductDemandCreateRequest request) {
        try {
            RawProductDemand created = rawProductDemandService.create(request.toEntity());
            return ResponseEntity.ok(ApiResponse.SUCCESS(HttpStatus.CREATED.toString(), "Raw product demand created", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{demandId}")
    public ResponseEntity<ApiResponse<RawProductDemand>> read(@PathVariable Long demandId) {
        try {
            RawProductDemand demand = rawProductDemandService.read(demandId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demand retrieved", demand));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RawProductDemand>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            List<RawProductDemand> demands = rawProductDemandService.readAll(pageNum, pageSize);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demands retrieved", demands));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @PutMapping("/{demandId}")
    public ResponseEntity<ApiResponse<RawProductDemand>> update(
            @PathVariable Long demandId,
            @RequestBody RawProductDemandUpdateRequest request) {
        try {
            RawProductDemand existing = rawProductDemandService.read(demandId);
            existing.setSubSubcategoryId(request.getSubSubcategoryId());
            existing.setUnit(request.getUnit());
            existing.setUnitQuantity(request.getUnitQuantity());
            existing.setUnitPrice(request.getUnitPrice());
            existing.setDateNeed(request.getDateNeed());
            existing.setNote(request.getNote());
            existing.setStatus(request.getStatus());
            RawProductDemand updated = rawProductDemandService.update(demandId, existing);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demand updated", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{demandId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long demandId) {
        try {
            rawProductDemandService.delete(demandId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Raw product demand deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.ERROR(HttpStatus.NOT_FOUND.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/by-category/{subSubcategoryId}")
    public ResponseEntity<ApiResponse<List<RawProductDemand>>> findBySubSubcategoryId(@PathVariable Long subSubcategoryId) {
        try {
            List<RawProductDemand> demands = rawProductDemandService.findBySubSubcategoryId(subSubcategoryId);
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Demands retrieved by category", demands));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.ERROR(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), null));
        }
    }

    @PostMapping("/{demandId}/confirm")
    public ResponseEntity<ApiResponse<RawProductDemand>> confirm(
            @PathVariable Long demandId,
            @RequestBody RawProductDemandConfirmRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        try {
            Long providerId = principal.getId();
            RawProductDemand updated = rawProductDemandService.confirmForProvider(
                    demandId, providerId, request.getQuantity(), request.getNote());
            return ResponseEntity.ok(ApiResponse.SUCCESS("OK", "Demand confirmed by provider", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
