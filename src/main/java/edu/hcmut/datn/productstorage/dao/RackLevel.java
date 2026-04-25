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
@Table(name = "rack_levels")
@NoArgsConstructor
public class RackLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rack_level_id")
    @Getter
    private Long rackLevelId;

    @Column(name = "usage_percentage")
    @Getter
    @Setter
    private Long usagePercentage;

    @Column(name = "rack_id")
    @Getter
    @Setter
    private Long rackId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RackLevel(Long usagePercentage, Long rackId) {
        this.usagePercentage = usagePercentage;
        this.rackId = rackId;
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
