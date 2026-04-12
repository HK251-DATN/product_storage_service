package edu.hcmut.datn.productstorage.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {
    
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "batch_detail_id")
    private Long batchDetailId;
    
    @Column(name = "buyer_id")
    private Long buyerId;
    
    @Column(name = "product_detail_id")
    private Long productDetailId;  // 1-to-1 relationship with ProductDetail
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructor for creating from event
    public OrderItem(Long orderItemId, Long orderId, Long batchDetailId, Long quantity,
                     BigDecimal unitPriceAtPurchase, Long buyerId) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.batchDetailId = batchDetailId;
        this.buyerId = buyerId;
    }
}