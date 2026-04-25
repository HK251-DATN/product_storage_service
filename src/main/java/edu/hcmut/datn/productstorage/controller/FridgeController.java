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

import edu.hcmut.datn.productstorage.dao.Fridge;
import edu.hcmut.datn.productstorage.dto.request.FridgeCreateRequest;
import edu.hcmut.datn.productstorage.dto.request.FridgeUpdateRequest;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.FridgeService;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
@RequestMapping("/api/fridge")
public class FridgeController {

    private final FridgeService fridgeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Fridge>> create(@RequestBody FridgeCreateRequest request) {
        try {
            Fridge newFridge = fridgeService.create(request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create fridge successfully", newFridge));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping("/{fridgeId}")
    public ResponseEntity<ApiResponse<Fridge>> read(@PathVariable Long fridgeId) {
        try {
            Fridge newFridge = fridgeService.read(fridgeId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read fridge successfully", newFridge));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Fridge>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        List<Fridge> fridges = fridgeService.readAll(pageNum, pageSize);

        if (fridges.isEmpty()) {
            return ResponseEntity.ok().body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "No fridge exists", null));
        }

        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get all fridges successfully", fridges));
    }

    @PutMapping("/{fridgeId}")
    public ResponseEntity<ApiResponse<Fridge>> update(
            @PathVariable Long fridgeId,
            @RequestBody FridgeUpdateRequest request
    ) {
        try {
            Fridge newFridge = fridgeService.update(fridgeId, request.toEntity());

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update fridge successfully", newFridge));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }

    @DeleteMapping("/{fridgeId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long fridgeId
    ) {
        try {
            fridgeService.delete(fridgeId);

            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete fridge successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
