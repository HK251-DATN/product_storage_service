package edu.hcmut.datn.productstorage.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;

import edu.hcmut.datn.productstorage.common.enums.StorageToolStatus;
import edu.hcmut.datn.productstorage.common.enums.StorageType;
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
@Table(name="storage_tools")
@NoArgsConstructor
public class StorageTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_tool_id")    
    private Long storageToolId;
    
    @Column(name = "last_maintainance_date")
    private LocalDate lastMaintainanceDate;
    
    @Column(name = "status")
    private StorageToolStatus status;
    
    @Column(name = "usage_percentage")
    private Long usagePercentage;
    
    @Column(name = "warehouse_id")
    private Long warehouseId;
    
    @Column(name = "tool_type")
    private StorageType toolType;

    @Column(name="created_at")
    private LocalDateTime createdAt;
    
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

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
