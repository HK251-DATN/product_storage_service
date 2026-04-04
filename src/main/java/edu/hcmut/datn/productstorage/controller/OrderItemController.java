package edu.hcmut.datn.productstorage.controller;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import edu.hcmut.datn.productstorage.dto.response.ApiResponse;
import edu.hcmut.datn.productstorage.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {
    
    private final OrderItemService orderItemService;
    
    /**
     * Get all order items
     * GET /api/order-items?pageNum=1&pageSize=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderItem>>> readAll(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Page<OrderItem> orderItemPage = orderItemService.readAll(pageNum, pageSize);
        
        if (orderItemPage.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(
                            HttpStatus.OK.toString(),
                            "No order items exist",
                            null));
        }
        
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(
                        HttpStatus.OK.toString(),
                        "Get all order items successfully",
                        orderItemPage.getContent()));
    }
    
    /**
     * Get order item by ID
     * GET /api/order-items/{orderItemId}
     */
    @GetMapping("/{orderItemId}")
    public ResponseEntity<ApiResponse<OrderItem>> read(@PathVariable Long orderItemId) {
        try {
            OrderItem orderItem = orderItemService.read(orderItemId);
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Read order item successfully",
                            orderItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null));
        }
    }
    
    /**
     * Get order items by order ID
     * GET /api/order-items/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderItem>>> readByOrderId(@PathVariable Long orderId) {
        List<OrderItem> orderItems = orderItemService.readByOrderId(orderId);
        
        if (orderItems.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(
                            HttpStatus.OK.toString(),
                            "No order items found for this order",
                            null));
        }
        
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(
                        HttpStatus.OK.toString(),
                        "Get order items by order ID successfully",
                        orderItems));
    }
    
    /**
     * Get pending order items (not assigned to product detail yet)
     * GET /api/order-items/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OrderItem>>> getPendingOrderItems() {
        List<OrderItem> pendingItems = orderItemService.getPendingOrderItems();
        
        if (pendingItems.isEmpty()) {
            return ResponseEntity.ok()
                    .body(ApiResponse.SKIP_AS_GOOD(
                            HttpStatus.OK.toString(),
                            "No pending order items",
                            null));
        }
        
        return ResponseEntity.ok()
                .body(ApiResponse.SUCCESS(
                        HttpStatus.OK.toString(),
                        "Get pending order items successfully",
                        pendingItems));
    }
    
    /**
     * Assign product detail to order item
     * PUT /api/order-items/{orderItemId}/assign/{productDetailId}
     */
    @PutMapping("/{orderItemId}/assign/{productDetailId}")
    public ResponseEntity<ApiResponse<OrderItem>> assignProductDetail(
            @PathVariable Long orderItemId,
            @PathVariable Long productDetailId) {
        try {
            OrderItem updatedOrderItem = orderItemService.assignProductDetail(
                    orderItemId,
                    productDetailId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(
                            HttpStatus.OK.toString(),
                            "Product detail assigned successfully",
                            updatedOrderItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(
                            HttpStatus.BAD_REQUEST.toString(),
                            e.getMessage(),
                            null));
        }
    }
}