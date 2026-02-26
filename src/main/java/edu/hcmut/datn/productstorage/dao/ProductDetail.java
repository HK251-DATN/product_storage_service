package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProductStatus;
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
@Table(name="product_details")
@NoArgsConstructor
public class ProductDetail {

    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="prod_detail_id")
    private Long prodDetailId;

    @Column(name="status")
    private ProductStatus status;

    @Column(name="price")
    private Long price;

    @Column(name="num_of_star")
    private Long numOfStar;

    @Column(name="created_at")
    private LocalDateTime createdAt;
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="storage_tool_id")
    private Long storageToolId;

    @Column(name="batch_id")
    private Long batchId;

    @Column(name="prod_gen_id")
    private Long prodGenId;
    
    @Column(name="unit")
    private Unit unit;

    @Column(name="unit_quantity")
    private Long unitQuantity;

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
