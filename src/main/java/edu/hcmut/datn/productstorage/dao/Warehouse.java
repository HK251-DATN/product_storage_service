package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="warehouses")
@NoArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    @Getter
    private Long warehouseId;

    @Column(name = "address")
    @Setter
    @Getter
    private String address;

    @Column(name = "usage_percentage")
    @Setter
    @Getter
    private Long usagePercentage;

    @Column(name = "num_of_fridge")
    @Setter
    @Getter
    private Long numOfFridge;

    @Column(name = "num_of_rack")
    @Setter
    @Getter
    private Long numOfRack;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public Warehouse(String address, Long usagePercentage, Long numOfFridge, Long numOfRack) {

        this.address = address;
        this.usagePercentage = usagePercentage;
        this.numOfFridge = numOfFridge;
        this.numOfRack = numOfRack;
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
