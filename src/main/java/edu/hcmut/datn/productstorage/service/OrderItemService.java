package edu.hcmut.datn.productstorage.service;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderItemService {
    
    /**
     * Get all order items with pagination
     */
    Page<OrderItem> readAll(Integer pageNum, Integer pageSize);
    
    /**
     * Get order item by ID
     */
    OrderItem read(Long orderItemId);
    
    /**
     * Get order items by order ID
     */
    List<OrderItem> readByOrderId(Long orderId);
    
    /**
     * Get order items by buyer ID
     */
    List<OrderItem> readByBuyerId(String buyerId);
    
    /**
     * Assign product detail to order item (1-to-1 relationship)
     * This is called when the physical product is collected
     */
    OrderItem assignProductDetail(Long orderItemId, Long productDetailId);
    
    /**
     * Get order items that haven't been assigned a product detail yet
     */
    List<OrderItem> getPendingOrderItems();
}