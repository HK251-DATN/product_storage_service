package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name="product_batchs")
@NoArgsConstructor
public class ProductBatch {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="batch_id")
    private Long batchId;

    @Column(name="quantity")
    private Long quantity;

    @Column(name="unit")
    private Unit unit;

    @Column(name="note")
    private String note;

    @Column(name="received_at")
    private LocalDateTime receivedAt;

    @Column(name="expired_at")
    private LocalDateTime expiredAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="updated_at")
    private LocalDateTime createdAt;

    @Column(name="provider_id")
    private Long providerId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(); // Set createdAt on first save
        updatedAt = LocalDateTime.now(); // Optional: Set initial updatedAt
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(); // Update on every save after creation
    }
}
