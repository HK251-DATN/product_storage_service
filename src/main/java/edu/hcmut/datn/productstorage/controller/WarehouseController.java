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

import edu.hcmut.datn.productstorage.dao.Warehouse;
import edu.hcmut.datn.productstorage.dto.request.WarehouseCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.WarehouseUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.WarehouseService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<ApiResponse<Warehouse>> create(@RequestBody WarehouseCreateRequest request) {
        try {
            Warehouse newWarehouse = warehouseService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create warehouse successfully", newWarehouse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<Warehouse>> read(@PathVariable Long warehouseId) {
        try {
            Warehouse newWarehouse = warehouseService.read(warehouseId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read warehouse successfully", newWarehouse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Warehouse>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<Warehouse> warehouses = warehouseService.readAll(pageNum, pageSize);

        if (warehouses.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No warehouse exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all warehouses successfully", warehouses));
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<Warehouse>> update(
            @PathVariable Long warehouseId,
            @RequestBody WarehouseUpdateRequest request
    ) {
        try {
            Warehouse newWarehouse = warehouseService.update(warehouseId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update warehouse successfully", newWarehouse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long warehouseId
    ) {
        try {
            warehouseService.delete(warehouseId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete warehouse successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
