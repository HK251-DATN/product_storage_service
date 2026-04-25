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
@Table(name="fridges")
@NoArgsConstructor
public class Fridge {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="fridge_id")
    @Getter
    private Long fridgeId;

    @Column(name="cur_temp")
    @Getter
    @Setter
    private Long curTemp;

    @Column(name="min_temp")
    @Getter
    @Setter
    private Long minTemp;

    @Column(name="max_temp")
    @Getter
    @Setter
    private Long maxTemp;

    @Column(name="storage_tool_id")
    @Getter
    @Setter
    private Long storageToolId;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    public Fridge(Long curTemp, Long minTemp, Long maxTemp, Long storageToolId) {

        this.curTemp = curTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
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
