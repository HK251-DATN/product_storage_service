package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.RawProductDemandStatus;
import edu.hcmut.datn.productstorage.common.enums.Unit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "raw_product_demands")
@NoArgsConstructor
public class RawProductDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "demand_id")
    @Getter
    private Long demandId;

    @Column(name = "sub_subcategory_id")
    @Setter
    @Getter
    private Long subSubcategoryId;

    @Column(name = "unit")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "unit_quantity")
    @Setter
    @Getter
    private Long unitQuantity;

    @Column(name = "unit_price")
    @Setter
    @Getter
    private Long unitPrice;

    @Column(name = "current_progress")
    @Setter
    @Getter
    private Long currentProgress;

    @Column(name = "date_need")
    @Setter
    @Getter
    private LocalDate dateNeed;

    @Column(name = "status")
    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private RawProductDemandStatus status;

    @Column(name = "note")
    @Setter
    @Getter
    private String note;

    @Column(name = "created_at")
    @Getter
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Getter
    private LocalDateTime updatedAt;

    public RawProductDemand(Long subSubcategoryId, Unit unit, Long unitQuantity, Long unitPrice, LocalDate dateNeed, String note) {
        this.subSubcategoryId = subSubcategoryId;
        this.unit = unit;
        this.unitQuantity = unitQuantity;
        this.unitPrice = unitPrice;
        this.dateNeed = dateNeed;
        this.note = note;
        this.currentProgress = 0L;
        this.status = RawProductDemandStatus.PENDING;
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
