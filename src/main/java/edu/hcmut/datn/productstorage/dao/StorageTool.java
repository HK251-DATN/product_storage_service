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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="storage_tools")
@NoArgsConstructor
public class StorageTool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storage_tool_id")
    @Getter
    private Long storageToolId;

    @Column(name = "last_maintainance_date")
    @Setter
    @Getter
    private LocalDate lastMaintainanceDate;

    @Column(name = "status")
    @Setter
    @Getter
    private StorageToolStatus status;

    @Column(name = "usage_percentage")
    @Setter
    @Getter
    private Long usagePercentage;

    @Column(name = "warehouse_id")
    @Setter
    @Getter
    private Long warehouseId;

    @Column(name = "tool_type")
    @Setter
    @Getter
    private StorageType toolType;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public StorageTool(LocalDate lastMaintainanceDate, StorageToolStatus status, Long usagePercentage, Long warehouseId, StorageType toolType) {
        this.lastMaintainanceDate = lastMaintainanceDate;
        this.status = status;
        this.usagePercentage = usagePercentage;
        this.warehouseId = warehouseId;
        this.toolType = toolType;
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
