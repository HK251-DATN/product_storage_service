package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="product_batchs")
@NoArgsConstructor
public class ProductBatch {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="batch_id")
    @Getter
    private Long batchId;

    @Column(name="quantity")
    @Setter
    @Getter
    private Long quantity;

    @Column(name="unit")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name="note")
    @Setter
    @Getter
    private String note;

    @Column(name="received_at")
    @Setter
    @Getter
    private LocalDateTime receivedAt;

    @Column(name="expired_at")
    @Setter
    @Getter
    private LocalDateTime expiredAt;

    @Column(name="process_status")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private ProductBatchProcessStatus processStatus;

    @Column(name="updated_at")
    @Getter
    private LocalDateTime updatedAt;

    @Column(name="created_at")
    @Getter
    private LocalDateTime createdAt;

    @Column(name="provider_id")
    @Setter
    @Getter
    private Long providerId;
    
    @Column(name = "sub_subcategory_id")
    @Setter
    @Getter
    private Long subSubcategoryId;

    public ProductBatch(Long quantity, Unit unit, String note, LocalDateTime receivedAt, LocalDateTime expiredAt, Long providerId, Long subSubcategoryId) {

        this.quantity = quantity;
        this.unit = unit;
        this.note = note;
        this.receivedAt = receivedAt;
        this.expiredAt = expiredAt;
        this.providerId = providerId;
        this.subSubcategoryId = subSubcategoryId;
        this.processStatus = ProductBatchProcessStatus.PENDING;
    }

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
