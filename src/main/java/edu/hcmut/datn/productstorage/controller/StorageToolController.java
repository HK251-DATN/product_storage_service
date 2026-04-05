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

import edu.hcmut.datn.productstorage.dao.StorageTool;
import edu.hcmut.datn.productstorage.dto.request.StorageToolCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.StorageToolUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.StorageToolService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/storage-tool")
public class StorageToolController {

    private final StorageToolService storageToolService;

    @PostMapping
    public ResponseEntity<ApiResponse<StorageTool>> create(@RequestBody StorageToolCreateRequest request) {
        try {
            StorageTool newStorageTool = storageToolService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create storageTool successfully", newStorageTool));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{storageToolId}")
    public ResponseEntity<ApiResponse<StorageTool>> read(@PathVariable Long storageToolId) {
        try {
            StorageTool newStorageTool = storageToolService.read(storageToolId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read storageTool successfully", newStorageTool));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StorageTool>>> readAll(
            @RequestParam(defaultValue = "0", name = "warehouse-id") Long wareHouseId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<StorageTool> storageTools = storageToolService.readAll(wareHouseId, pageNum, pageSize);

        if (storageTools.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No storageTool exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all storageTools successfully", storageTools));
    }

    @PutMapping("/{storageToolId}")
    public ResponseEntity<ApiResponse<StorageTool>> update(
            @PathVariable Long storageToolId,
            @RequestBody StorageToolUpdateRequest request
    ) {
        try {
            StorageTool newStorageTool = storageToolService.update(storageToolId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update storageTool successfully", newStorageTool));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{storageToolId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long storageToolId
    ) {
        try {
            storageToolService.delete(storageToolId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete storageTool successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
