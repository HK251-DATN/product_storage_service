package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.hcmut.datn.productstorage.common.enums.ProductBatchProcessStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_sub_batchs")
@NoArgsConstructor
public class ProductSubBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_batch_id")
    @Getter
    private Long subBatchId;

    @Column(name = "quantity")
    @Setter
    @Getter
    private Long quantity;

    @Column(name = "unit")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "note")
    @Setter
    @Getter
    private String note;

    @Column(name = "received_at")
    @Setter
    @Getter
    private LocalDateTime receivedAt;

    @Column(name = "expired_at")
    @Setter
    @Getter
    private LocalDateTime expiredAt;

    @Column(name = "process_status")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private ProductBatchProcessStatus processStatus;

    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    @Getter
    private LocalDateTime createdAt;

    @Column(name = "provider_id")
    @Setter
    @Getter
    private Long providerId;

    @Column(name = "sub_subcategory_id")
    @Setter
    @Getter
    private Long subSubcategoryId;

    @Column(name = "product_batch_id")
    @Setter
    @Getter
    private Long productBatchId;

    @Column(name = "raw_product_demand_id")
    @Setter
    @Getter
    private Long rawProductDemandId;

    @ElementCollection
    @CollectionTable(name = "product_sub_batch_proof_images", joinColumns = @JoinColumn(name = "sub_batch_id"))
    @Column(name = "image_url")
    @Setter
    @Getter
    private List<String> proofImageUrls = new ArrayList<>();

    public ProductSubBatch(Long quantity, Unit unit, String note, LocalDateTime receivedAt, LocalDateTime expiredAt, Long providerId, Long subSubcategoryId, Long productBatchId, Long rawProductDemandId) {
        this.quantity = quantity;
        this.unit = unit;
        this.note = note;
        this.receivedAt = receivedAt;
        this.expiredAt = expiredAt;
        this.providerId = providerId;
        this.subSubcategoryId = subSubcategoryId;
        this.productBatchId = productBatchId;
        this.rawProductDemandId = rawProductDemandId;
        this.processStatus = ProductBatchProcessStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
