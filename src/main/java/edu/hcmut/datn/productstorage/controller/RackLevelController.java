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

import edu.hcmut.datn.productstorage.dao.RackLevel;
import edu.hcmut.datn.productstorage.dto.request.RackLevelCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.RackLevelUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.RackLevelService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/rack-level")
public class RackLevelController {

    private final RackLevelService rackLevelService;

    @PostMapping
    public ResponseEntity<ApiResponse<RackLevel>> create(@RequestBody RackLevelCreateRequest request) {
        try {
            RackLevel newRackLevel = rackLevelService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create rackLevel successfully", newRackLevel));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{rackLevelId}")
    public ResponseEntity<ApiResponse<RackLevel>> read(@PathVariable Long rackLevelId) {
        try {
            RackLevel newRackLevel = rackLevelService.read(rackLevelId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read rackLevel successfully", newRackLevel));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RackLevel>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<RackLevel> rackLevels = rackLevelService.readAll(pageNum, pageSize);

        if (rackLevels.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No rackLevel exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all rackLevels successfully", rackLevels));
    }

    @PutMapping("/{rackLevelId}")
    public ResponseEntity<ApiResponse<RackLevel>> update(
            @PathVariable Long rackLevelId,
            @RequestBody RackLevelUpdateRequest request
    ) {
        try {
            RackLevel newRackLevel = rackLevelService.update(rackLevelId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update rackLevel successfully", newRackLevel));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{rackLevelId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long rackLevelId
    ) {
        try {
            rackLevelService.delete(rackLevelId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete rackLevel successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
