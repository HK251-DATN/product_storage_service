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
import lombok.NoArgsConstructor;

@Entity
@Table(name="fridges")
@NoArgsConstructor
public class Fridge {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="fridge_id")
    private Long fridgeId;

    @Column(name="cur_temp")
    private Long curTemp;

    @Column(name="min_temp")
    private Long minTemp;

    @Column(name="max_temp")
    private Long maxTemp;

    @Column(name="storage_tool_id")
    private Long storageToolId;

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
