package edu.hcmut.datn.productstorage.repository;

import edu.hcmut.datn.productstorage.dao.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * Find order items by order ID
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Find order item by batch detail ID
     */
    Optional<OrderItem> findByBatchDetailId(Long batchDetailId);
    
    /**
     * Find order items by buyer ID
     */
    List<OrderItem> findByBuyerId(String buyerId);
    
    /**
     * Check if order item exists
     */
    boolean existsByOrderItemId(Long orderItemId);
}