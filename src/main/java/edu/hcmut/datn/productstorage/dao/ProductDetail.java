package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.ProductStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_details")
@NoArgsConstructor
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_detail_id")
    @Getter
    private Long prodDetailId;

    @Column(name = "status")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "price")
    @Getter
    @Setter
    private Long price;

    @Column(name = "num_of_star")
    @Getter
    @Setter
    private Long numOfStar;

    @Column(name = "created_at")
    @Getter
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    @Column(name = "storage_tool_id")
    @Getter
    @Setter
    private Long storageToolId;

    @Column(name = "batch_id")
    @Getter
    @Setter
    private Long batchId;

    @Column(name = "prod_gen_id")
    @Getter
    @Setter
    private Long prodGenId;

    @Column(name = "unit")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "unit_quantity")
    @Getter
    @Setter
    private Long unitQuantity;

    public ProductDetail(ProductStatus status, Long price, Long numOfStar, Long storageToolId, Long batchId, Long prodGenId, Unit unit, Long unitQuantity) {

        this.status = status;
        this.price = price;
        this.numOfStar = numOfStar;
        this.storageToolId = storageToolId;
        this.batchId = batchId;
        this.prodGenId = prodGenId;
        this.unit = unit;
        this.unitQuantity = unitQuantity;
    }

    public ProductDetail copy() {
        ProductDetail productDetail = new ProductDetail();

        productDetail.setStatus(status);
        productDetail.setPrice(price);
        productDetail.setNumOfStar(numOfStar);
        productDetail.setStorageToolId(storageToolId);
        productDetail.setBatchId(batchId);
        productDetail.setProdGenId(prodGenId);
        productDetail.setUnit(unit);
        productDetail.setUnitQuantity(unitQuantity);

        return productDetail;
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
