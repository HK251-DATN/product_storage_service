package edu.hcmut.datn.productstorage.controller;

import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.repository.projector.PickListItem;
import edu.hcmut.datn.productstorage.repository.projector.ProductDetailForPickItem;
import edu.hcmut.datn.productstorage.service.PickListService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pick-list")
@AllArgsConstructor
public class PickListController {

    private final PickListService pickListService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<List<PickListItem>>> getPickList(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Get pick list for order %d successfully".formatted(orderId),
                            pickListService.getPickList(orderId)
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    )
            );
        }
    }

    @PutMapping("/{orderItemId}/link/{productDetailId}")
    public ResponseEntity<?> linkOrderItem(@PathVariable Long orderItemId, @PathVariable Long productDetailId) {
        try {
            pickListService.linkOrderItem(orderItemId, productDetailId);
            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Link order item %d with product detail %d successfully".formatted(orderItemId, productDetailId),
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    )
            );
        }
    }
    
    @GetMapping("/product-detail-list/{orderItemId}")
    public ResponseEntity<?> getProductDetailListForOrderItem(
            @PathVariable Long orderItemId
    ) {
        try {
            List<ProductDetailForPickItem> productDetailForPickItems = pickListService.getProductDetailListForPickItem(orderItemId);
            return ResponseEntity.ok().body(
                    ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Get available products for order item %d successfully".formatted(orderItemId),
                            productDetailForPickItems
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null
                    )
            );
        }
    }
}
