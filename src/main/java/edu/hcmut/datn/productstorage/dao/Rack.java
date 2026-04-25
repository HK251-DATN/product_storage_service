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
@Table(name = "rack")
@NoArgsConstructor
public class Rack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rack_id")
    @Getter
    private Long rackId;

    @Column(name = "num_of_level")
    @Getter
    @Setter
    private Long numOfLevel;

    @Column(name = "storage_tool_id")
    @Getter
    @Setter
    private Long storageToolId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Rack(Long numOfLevel, Long storageToolId) {
        this.numOfLevel = numOfLevel;
        this.storageToolId = storageToolId;
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
